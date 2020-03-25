package ca.szc.maven.jsonpath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

@Mojo(name="modify", defaultPhase=LifecyclePhase.PROCESS_RESOURCES)
public class ModifyMojo extends AbstractMojo
{
    @Parameter(property = "jsonpath.file", required = true)
    private String file;

    @Parameter(property = "jsonpath.outputFile", required = false)
    private String outputFile;

    @Parameter(property = "jsonpath.formatter", defaultValue = "conventional", required = false)
    private String formatter;

    @Parameter(required = true)
    private List<ModifyMojoModifications> modifications;

    public void execute() throws MojoExecutionException
    {
        FileSystem fs = FileSystems.getDefault();
        Path inputJson = fs.getPath(file);
        Path outputJson = outputFile == null ? inputJson : fs.getPath(outputFile);

        Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();

        DocumentContext json;
        try (InputStream in = Files.newInputStream(inputJson))
        {
            json = JsonPath.using(configuration).parse(in, "UTF-8");
        }
        catch (IOException e)
        {
            getLog().error("Unable to read input json file");
            throw new MojoExecutionException("Unable to read file '" + file + "'", e);
        }

        int count = 0;

        for (ModifyMojoModifications modification : modifications)
        {
            String expression = modification.getExpression();
            String value = modification.getValue();
            json.set(expression, value);
            getLog().info(expression + "=" + value);
            count++;
        }

        try (OutputStream out = Files.newOutputStream(outputJson))
        {
            PrettyPrinter prettyPrinter;
            if ("conventional".equals(formatter)) {
                prettyPrinter = new ConventionalPrettyPrinter();
            } else if ("jackson".equals(formatter)) {
                prettyPrinter = new DefaultPrettyPrinter();
            } else {
                getLog().error("Invalid JSON formatter specified");
                throw new MojoExecutionException("Unknown formatter '" + formatter + "'");
            }
            ObjectWriter writer = new ObjectMapper().writer(prettyPrinter);
            writer.writeValue(out, json.json());
        }
        catch (IOException e)
        {
            getLog().error("Unable to write output json file");
            throw new MojoExecutionException("Unable write file '" + outputJson + "'", e);
        }

        if (count == 0)
        {
            getLog().error(count + " modifications written to json file " + outputJson);
            throw new MojoExecutionException("No properties were defined for setting");
        }
        getLog().info(count + " modifications written to json file " + outputJson);
    }
}

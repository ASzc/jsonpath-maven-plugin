package ca.szc.maven.jsonpath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

@Mojo(name="set-properties", defaultPhase=LifecyclePhase.INITIALIZE)
public class SetPropertiesMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(property = "jsonpath.file", required = true)
    private String file;

    @Parameter(required = true)
    private Map<String, String> properties;

    public void execute() throws MojoExecutionException
    {
        FileSystem fs = FileSystems.getDefault();
        Path inputJson = fs.getPath(file);

        Object json;

        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);
        try (InputStream in = Files.newInputStream(inputJson))
        {
            json = conf.jsonProvider().parse(in, "UTF-8");
        }
        catch (IOException e)
        {
            getLog().error("Unable to read input json file");
            throw new MojoExecutionException("Unable to read file '" + file + "'", e);
        }

        DocumentContext context = JsonPath.parse(json, conf);

        int count = 0;

        Properties sessionProperties = session.getUserProperties();
        for (Entry<String, String> entry : properties.entrySet())
        {
            String propertyName = entry.getKey();
            String propertyJsonPath = entry.getValue();
            getLog().debug("Reading value for " + propertyName + " with JsonPath expression " + propertyJsonPath);
            List<String> propertyValues = context.read(propertyJsonPath);
            if (propertyValues.size() != 1) {
                getLog().error("More than 1 value found for indefinite JsonPath expression: " + propertyJsonPath);
                throw new MojoExecutionException("More than 1 value found for indefinite JsonPath expression: " + propertyJsonPath);
            }
            String propertyValue = propertyValues.get(0);
            sessionProperties.setProperty(propertyName, propertyValue);
            getLog().info(propertyName + "=" + propertyValue);
            count++;
        }

        if (count == 0)
        {
            getLog().error(count + " build properties set from json file " + file);
            throw new MojoExecutionException("No properties were defined for setting");
        }
        getLog().info(count + " build properties set from json file " + file);
    }
}

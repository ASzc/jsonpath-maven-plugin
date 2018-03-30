package ca.szc.maven.jsonpath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

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
        try (InputStream in = Files.newInputStream(inputJson))
        {
            json = Configuration.defaultConfiguration().jsonProvider().parse(in, "UTF-8");
        }
        catch (IOException e)
        {
            getLog().error("Unable to read input json file");
            throw new MojoExecutionException("Unable to read file '" + file + "'", e);
        }

        int count = 0;

        Properties sessionProperties = session.getUserProperties();
        for (Entry<String, String> entry : properties.entrySet())
        {
            String propertyName = entry.getKey();
            String propertyJsonPath = entry.getValue();
            getLog().debug("Reading value for " + propertyName + " with JsonPath expression " + propertyJsonPath);
            String propertyValue = JsonPath.read(json, propertyJsonPath);
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

package ca.szc.maven.jsonpath;

import org.apache.maven.plugins.annotations.Parameter;

public class ModifyMojoModifications
{
    @Parameter(required = true)
    private String expression;

    public String getExpression()
    {
        return expression;
    }

    @Parameter(required = true)
    private String value;

    public String getValue()
    {
        return value;
    }
}

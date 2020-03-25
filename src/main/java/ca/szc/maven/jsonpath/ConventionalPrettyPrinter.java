package ca.szc.maven.jsonpath;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;

public class ConventionalPrettyPrinter extends DefaultPrettyPrinter
{
    private static final long serialVersionUID = 1;

    public ConventionalPrettyPrinter() {
        super();
        this._arrayIndenter = this._objectIndenter;
    }

    @Override
    public ConventionalPrettyPrinter withSeparators(Separators separators)
    {
        this._separators = separators;
        _objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
        return this;
    }

    @Override
    public ConventionalPrettyPrinter createInstance()
    {
        return new ConventionalPrettyPrinter();
    }
}

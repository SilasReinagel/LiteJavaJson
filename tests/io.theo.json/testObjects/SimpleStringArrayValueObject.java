package io.theo.json.testObjects;

public class SimpleStringArrayValueObject
{
    public String[] Value;

    public SimpleStringArrayValueObject()
    {
    }

    public SimpleStringArrayValueObject(final String... values)
    {
        Value = values;
    }
}

package io.theo.json.testObjects;

import java.util.Map;

public class SimpleStringMapValueObject
{
    public final Map<String, String> Value;

    public SimpleStringMapValueObject(final Map<String, String> value)
    {
        Value = value;
    }
}

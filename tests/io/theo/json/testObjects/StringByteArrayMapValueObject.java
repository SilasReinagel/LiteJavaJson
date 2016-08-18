package io.theo.json.testObjects;

import java.util.Map;

public class StringByteArrayMapValueObject
{
    public final Map<String, byte[]> Value;

    public StringByteArrayMapValueObject(final Map<String, byte[]> value)
    {
        Value = value;
    }
}

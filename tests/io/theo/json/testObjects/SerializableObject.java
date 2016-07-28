package io.theo.json.testObjects;

import java.io.Serializable;

public class SerializableObject implements Serializable
{
    private static final long serialVersionUID = -2664966508060651537L;

    public String Value;

    public SerializableObject(final String value)
    {
        Value = value;
    }
}

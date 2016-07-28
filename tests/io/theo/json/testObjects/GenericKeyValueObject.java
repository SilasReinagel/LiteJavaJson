package io.theo.json.testObjects;

public class GenericKeyValueObject<T>
{
    public String Key;
    public T Value;

    public GenericKeyValueObject(final String key, final T value)
    {
        Key = key;
        Value = value;
    }
}

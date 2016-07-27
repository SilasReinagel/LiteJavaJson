package io.theo.json.testObjects;

public class SimpleSubObjectValueObject
{
    public SimpleIntegerValueObject Value;

    public SimpleSubObjectValueObject()
    {
    }

    public SimpleSubObjectValueObject(final SimpleIntegerValueObject obj)
    {
        Value = obj;
    }
}

package io.theo.json.testObjects;

import java.util.List;

public class TwoCollectionValueObject
{
    public final List<String> Value1;
    public final List<String> Value2;

    public TwoCollectionValueObject(final List<String> value1, final List<String> value2)
    {
        Value1 = value1;
        Value2 = value2;
    }
}

package io.theo.json.testObjects;

import java.util.Arrays;
import java.util.List;

public class CustomObjListValueObject
{
    public List<NumericTypesObject> Value;

    public CustomObjListValueObject(final NumericTypesObject... values)
    {
        Value = Arrays.asList(values);
    }
}

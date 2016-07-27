package io.theo.json.testObjects;

public class IntegerArrayValueObject
{
    public int[] Value1;
    public Integer[] Value2;

    public IntegerArrayValueObject()
    {
        Value1 = new int[0];
    }

    public IntegerArrayValueObject(final int[] value1, final Integer[] value2)
    {
        Value1 = value1;
        Value2 = value2;
    }
}

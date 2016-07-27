package io.theo.json.testObjects;

public class NumericTypesObject
{
    public int intValue;
    public long longValue;
    public float floatValue;
    public double dblValue;

    public NumericTypesObject()
    {
    }

    public NumericTypesObject(int intValue, long longValue, float floatValue, double dblValue)
    {
        this.intValue = intValue;
        this.longValue = longValue;
        this.dblValue = dblValue;
        this.floatValue = floatValue;
    }
}
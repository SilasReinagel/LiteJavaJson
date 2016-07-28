package io.theo.json.testObjects;

public class ChildClass extends ParentClass
{
    public String ChildValue;

    public ChildClass(final String childValue, final String parentValue)
    {
        super(parentValue);
        ChildValue = childValue;
    }
}

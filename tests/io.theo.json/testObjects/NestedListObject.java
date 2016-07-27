package io.theo.json.testObjects;

import java.util.ArrayList;
import java.util.List;

public class NestedListObject
{
    public List<List<List<List<Integer>>>> Value;

    public NestedListObject()
    {
    }

    public NestedListObject(final List<Integer> innerValues)
    {
        Value = new ArrayList<>();
        List<List<List<List<Integer>>>> list4 = new ArrayList<>();
        List<List<List<Integer>>> list3 = new ArrayList<>();
        List<List<Integer>> list2 = new ArrayList<>();
        list4.add(list3);
        list3.add(list2);
        list2.add(innerValues);
    }

    public List<Integer> getInnerValues()
    {
        if (Value == null)
            throw new IllegalStateException();
        return Value.get(0).get(0).get(0);
    }
}

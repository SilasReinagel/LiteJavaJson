package io.theo.json;

import io.theo.json.testObjects.*;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;

public class JsonSerializerTests
{
    @Test
    public void JsonSerializer_ToJsonNull_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleStringValueObject(null));

        Assert.assertEquals("{ \"Value\": null }", json);
    }

    @Test
    public void JsonSerializer_ToJsonBoolean_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleBooleanValueObject(true, false));

        Assert.assertEquals("{ \"Value1\": true, \"Value2\": false }", json);
    }

    @Test
    public void JsonSerializer_ToJsonNumeric_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new NumericTypesObject(1, 2, 3.1f, 4.5));

        Assert.assertEquals("{ \"intValue\": 1, \"longValue\": 2, \"floatValue\": 3.1, \"dblValue\": 4.5 }", json);
    }

    @Test
    public void JsonSerializer_ToJsonStringData_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleStringValueObject("SampleValue"));

        Assert.assertEquals("{ \"Value\": \"SampleValue\" }", json);
    }

    @Test
    public void JsonSerializer_ToJsonEnumData_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleEnumValueObject(SampleEnum.Value2));

        Assert.assertEquals("{ \"Value\": \"Value2\" }", json);
    }

    @Test
    public void JsonSerializer_ToJsonNestedObject_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleSubObjectValueObject(new SimpleIntegerValueObject(1, 3)));

        Assert.assertEquals("{ \"Value\": { \"Value1\": 1, \"Value2\": 3 } }", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromList_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleStringListValueObject(Arrays.asList("JC Denton", "Adam Jensen")));

        Assert.assertEquals("{ \"Value\": [ \"JC Denton\", \"Adam Jensen\" ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromEmptyList_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleStringListValueObject(new ArrayList<>()));

        Assert.assertEquals("{ \"Value\": [ ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromIntArray_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new IntegerArrayValueObject(new int[] { 4, 3, 2, 1 }, new Integer[] { 4 }));

        Assert.assertEquals("{ \"Value1\": [ 4, 3, 2, 1 ], \"Value2\": [ 4 ] }", json);
    }
}

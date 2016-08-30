package io.theo.json;

import io.theo.json.testObjects.*;
import org.junit.*;

import java.util.*;
import java.util.stream.IntStream;

public class JsonSerializerTests
{
    @Test
    public void JsonSerializer_ToJsonNull_IsCorrect()
    {
        String json = Json.toJsonString(new SimpleStringValueObject(null));

        Assert.assertEquals("{ \"Value\": null }", json);
    }

    @Test
    public void JsonSerializer_ToJsonSingleValueBoolean_IsCorrect()
    {
        String json = Json.toJsonString(true);

        Assert.assertEquals("true", json);
    }

    @Test
    public void JsonSerializer_ToJsonSingleValueNumeric_IsCorrect()
    {
        String json = Json.toJsonString(1.2345);

        Assert.assertEquals("1.2345", json);
    }

    @Test
    public void JsonSerializer_ToJsonSingleValueString_IsCorrect()
    {
        String json = Json.toJsonString("John Doe");

        Assert.assertEquals("\"John Doe\"", json);
    }

    // Not part of the official JSON Spec. There is no specified way of handling byte arrays.
    @Test
    public void JsonSerializer_ToJsonSingleValueByteArray_EncodedAsBase64String()
    {
        byte[] bytes = new byte[]{1,2,3};

        String json = Json.toJsonString(bytes);

        String expected = "\"" + Base64.getEncoder().encodeToString(bytes) + "\"";
        Assert.assertEquals(expected, json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromJavaArray_IsCorrect()
    {
        String json = Json.toJsonString(new int[]{1, 2, 3, 4, 5});

        Assert.assertEquals("[ 1, 2, 3, 4, 5 ]", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromList_IsCorrect()
    {
        String json = Json.toJsonString(Arrays.asList("Superman", "Spiderman", "Batman"));

        Assert.assertEquals("[ \"Superman\", \"Spiderman\", \"Batman\" ]", json);
    }

    @Test
    public void JsonSerializer_ToJsonBooleanData_IsCorrect()
    {
        String json = Json.toJsonString(new SimpleBooleanValueObject(true, false));

        Assert.assertEquals("{ \"Value1\": true, \"Value2\": false }", json);
    }

    @Test
    public void JsonSerializer_ToJsonNumericData_IsCorrect()
    {
        String json = Json.toJsonString(new NumericTypesObject(1, 2, 3.1f, 4.5));

        Assert.assertEquals("{ \"intValue\": 1, \"longValue\": 2, \"floatValue\": 3.1, \"dblValue\": 4.5 }", json);
    }

    @Test
    public void JsonSerializer_ToJsonStringData_IsCorrect()
    {
        String json = Json.toJsonString(new SimpleStringValueObject("SampleValue"));

        Assert.assertEquals("{ \"Value\": \"SampleValue\" }", json);
    }

    @Test
    public void JsonSerializer_ToJsonEnumData_IsCorrect()
    {
        String json = Json.toJsonString(new SimpleEnumValueObject(SampleEnum.Value2));

        Assert.assertEquals("{ \"Value\": \"Value2\" }", json);
    }

    @Test
    public void JsonSerializer_ToJsonNestedObject_IsCorrect()
    {
        String json = Json.toJsonString(new SimpleSubObjectValueObject(new SimpleIntegerValueObject(1, 3)));

        Assert.assertEquals("{ \"Value\": { \"Value1\": 1, \"Value2\": 3 } }", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromInnerList_IsCorrect()
    {
        String json = Json.toJsonString(new SimpleStringListValueObject(Arrays.asList("JC Denton", "Adam Jensen")));

        Assert.assertEquals("{ \"Value\": [ \"JC Denton\", \"Adam Jensen\" ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromInnerEmptyList_IsCorrect()
    {
        String json = Json.toJsonString(new SimpleStringListValueObject(new ArrayList<>()));

        Assert.assertEquals("{ \"Value\": [ ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromInnerIntArray_IsCorrect()
    {
        String json = Json.toJsonString(new IntegerArrayValueObject(new int[] { 4, 3, 2, 1 }, new Integer[] { 4 }));

        Assert.assertEquals("{ \"Value1\": [ 4, 3, 2, 1 ], \"Value2\": [ 4 ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonGenericData_IsCorrect()
    {
        String json = Json.toJsonString(new GenericKeyValueObject<>("Price", 995.95));

        Assert.assertEquals("{ \"Key\": \"Price\", \"Value\": 995.95 }", json);
    }

    @Test
    public void JsonSerializer_ToJsonGenericDataCustomClass_IsCorrect()
    {
        String json = Json.toJsonString(new GenericKeyValueObject<>("obj", new SimpleStringValueObject("Content")));

        Assert.assertEquals("{ \"Key\": \"obj\", \"Value\": { \"Value\": \"Content\" } }", json);
    }

    @Test
    public void JsonSerializer_ToJsonPrivateFieldData_IsCorrect()
    {
        String json = Json.toJsonString(new SimplePrivateFieldClass("America"));

        Assert.assertEquals("{ \"value\": \"America\" }", json);
    }

    @Test
    public void JsonSerializer_ToJsonStaticFieldData_NotIncluded()
    {
        String json = Json.toJsonString(new SerializableObject("Pikachu"));

        Assert.assertEquals("{ \"Value\": \"Pikachu\" }", json);
    }

    @Test
    public void JsonSerializer_ToJsonMapDataSingleEntry_IsCorrect()
    {
        String json = Json.toJsonString(new SimpleStringMapValueObject(Collections.singletonMap("SampleName", "SampleValue")));

        Assert.assertEquals("{ \"Value\": [ { \"SampleName\": \"SampleValue\" } ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonMapDataMultipleEntries_IsCorrect()
    {
        Map<String, Integer> map = new HashMap<>();
        map.put("Adam", 1);
        map.put("Eve", 2);
        String json = Json.toJsonString(map);

        Assert.assertEquals("[ { \"Adam\": 1 }, { \"Eve\": 2 } ]", json);
    }

    @Test
    public void JsonSerializer_ToJsonTwoCollectionsWithOneEmpty_IsCorrect()
    {
        String json = Json.toJsonString(new TwoCollectionValueObject(Collections.singletonList("123"), new ArrayList<>()));

        Assert.assertEquals("{ \"Value1\": [ \"123\" ], \"Value2\": [ ] }", json);
    }
}

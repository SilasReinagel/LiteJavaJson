package io.theo.json;

import io.theo.json.testObjects.*;
import org.junit.*;

import java.util.*;
import java.util.stream.IntStream;

public class JsonSerializerTests
{
    private Random _rnd = new Random();

    @Test
    public void JsonSerializer_ToJsonNull_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleStringValueObject(null));

        Assert.assertEquals("{ \"Value\": null }", json);
    }

    @Test
    public void JsonSerializer_ToJsonSingleValueBoolean_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(true);

        Assert.assertEquals("true", json);
    }

    @Test
    public void JsonSerializer_ToJsonSingleValueNumeric_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(1.2345);

        Assert.assertEquals("1.2345", json);
    }

    @Test
    public void JsonSerializer_ToJsonSingleValueString_IsCorrect()
    {
        String json = JsonSerializer.toJsonString("John Doe");

        Assert.assertEquals("\"John Doe\"", json);
    }

    // Not part of the official JSON Spec. There is no specified way of handling byte arrays.
    @Test
    public void JsonSerializer_ToJsonSingleValueByteArray_EncodedAsBase64String()
    {
        byte[] bytes = new byte[]{1,2,3};

        String json = JsonSerializer.toJsonString(bytes);

        String expected = "\"" + Base64.getEncoder().encodeToString(bytes) + "\"";
        Assert.assertEquals(expected, json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromJavaArray_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new int[]{1, 2, 3, 4, 5});

        Assert.assertEquals("[ 1, 2, 3, 4, 5 ]", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromList_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(Arrays.asList("Superman", "Spiderman", "Batman"));

        Assert.assertEquals("[ \"Superman\", \"Spiderman\", \"Batman\" ]", json);
    }

    @Test
    public void JsonSerializer_ToJsonBooleanData_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleBooleanValueObject(true, false));

        Assert.assertEquals("{ \"Value1\": true, \"Value2\": false }", json);
    }

    @Test
    public void JsonSerializer_ToJsonNumericData_IsCorrect()
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
    public void JsonSerializer_ToJsonArrayFromInnerList_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleStringListValueObject(Arrays.asList("JC Denton", "Adam Jensen")));

        Assert.assertEquals("{ \"Value\": [ \"JC Denton\", \"Adam Jensen\" ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromInnerEmptyList_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleStringListValueObject(new ArrayList<>()));

        Assert.assertEquals("{ \"Value\": [ ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonArrayFromInnerIntArray_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new IntegerArrayValueObject(new int[] { 4, 3, 2, 1 }, new Integer[] { 4 }));

        Assert.assertEquals("{ \"Value1\": [ 4, 3, 2, 1 ], \"Value2\": [ 4 ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonGenericData_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new GenericKeyValueObject<>("Price", 995.95));

        Assert.assertEquals("{ \"Key\": \"Price\", \"Value\": 995.95 }", json);
    }

    @Test
    public void JsonSerializer_ToJsonGenericDataCustomClass_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new GenericKeyValueObject<>("obj", new SimpleStringValueObject("Content")));

        Assert.assertEquals("{ \"Key\": \"obj\", \"Value\": { \"Value\": \"Content\" } }", json);
    }

    @Test
    public void JsonSerializer_ToJsonPrivateFieldData_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimplePrivateFieldClass("America"));

        Assert.assertEquals("{ \"value\": \"America\" }", json);
    }

    @Test
    public void JsonSerializer_ToJsonStaticFieldData_NotIncluded()
    {
        String json = JsonSerializer.toJsonString(new SerializableObject("Pikachu"));

        Assert.assertEquals("{ \"Value\": \"Pikachu\" }", json);
    }

    @Test
    public void JsonSerializer_ToJsonMapDataSingleEntry_IsCorrect()
    {
        String json = JsonSerializer.toJsonString(new SimpleStringMapValueObject(Collections.singletonMap("SampleName", "SampleValue")));

        Assert.assertEquals("{ \"Value\": [ { \"SampleName\": \"SampleValue\" } ] }", json);
    }

    @Test
    public void JsonSerializer_ToJsonMapDataMultipleEntries_IsCorrect()
    {
        Map<String, Integer> map = new HashMap<>();
        map.put("Adam", 1);
        map.put("Eve", 2);
        String json = JsonSerializer.toJsonString(map);

        Assert.assertEquals("[ { \"Adam\": 1 }, { \"Eve\": 2 } ]", json);
    }

    @Test
    public void JsonSerializer_PerformanceTest_BetterThanTenThousandsOpsPerSecond()
    {
        SimpleStringListValueObject obj = new SimpleStringListValueObject(Arrays.asList("JC Denton", "Adam Jensen", "Paul Denton", "David Sarif"));

        double opsPerSecond = PerformanceTester.getOpsPerSecond(2000, 500, () -> JsonSerializer.toJsonString(obj));

        System.out.println("Simple POJO: " + (long)opsPerSecond + " ops/s");
        Assert.assertTrue(opsPerSecond > 10000);
    }

    @Test
    public void JsonSerializer_PerformanceTestLargeList_BetterThanTenThousandOpsPerSecond()
    {
        List<String> items = new ArrayList<>();
        IntStream.range(0, 100).forEach(x -> items.add(UUID.randomUUID().toString()));

        double opsPerSecond = PerformanceTester.getOpsPerSecond(2000, 500, () -> JsonSerializer.toJsonString(items));

        System.out.println("100 Item List: " + (long)opsPerSecond + " ops/s");
        Assert.assertTrue(opsPerSecond > 10000);
    }

    @Test
    public void JsonSerializer_PerformanceTestLargeMapOfByteArray_BetterThanFiveThousandOpsPerSecond()
    {
        Map<String, byte[]> items = new HashMap<>();
        IntStream.range(0, 100).forEach(x -> items.put(Integer.toString(x), getRandomBytes(100)));

        double opsPerSecond = PerformanceTester.getOpsPerSecond(2000, 500, () -> JsonSerializer.toJsonString(items));

        System.out.println("100 Item Byte Array Map: " + (long)opsPerSecond + " ops/s");
        Assert.assertTrue(opsPerSecond > 5000);
    }

    private byte[] getRandomBytes(int nBytes)
    {
        byte[] bytes = new byte[nBytes];
        for (int i = 0; i < nBytes; i ++)
            bytes[i] = (byte)_rnd.nextInt(128);
        return bytes;
    }
}

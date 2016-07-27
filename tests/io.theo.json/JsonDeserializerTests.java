package io.theo.json;

import io.theo.json.testObjects.*;
import org.junit.*;

import java.time.LocalDateTime;
import java.util.Arrays;

public class JsonDeserializerTests
{
    @Test
    public void JsonDeserializer_GetElementValue_IsCorrect()
    {
        double price = JsonDeserializer.getElementValue(Double.class, "Price",
                "{ \"Data1\": \"Something Useless\", \"Price\": 2.57 }");

        Assert.assertEquals(2.57, price, 0.01);
    }

    @Test
    public void JsonDeserializer_GetElementRawValue_IsCorrect()
    {
        String price = JsonDeserializer.getElementRawValue("Price",
                "{ \"Data1\": \"Something Useless\", \"Price\": 2.57 }");

        Assert.assertEquals("2.57", price);
    }

    @Test
    public void JsonDeserializer_ToObjWithoutDefaultConstructor_ThrowsException()
    {
        ExceptionAssert.assertThrows(RuntimeException.class, () -> JsonDeserializer.toObj(SimpleNonDefaultConstructorObject.class, ""));
    }

    @Test
    public void JsonDeserializer_JsonStringMissingCurlyBraces_ThrowsException()
    {
        ExceptionAssert.assertThrows(RuntimeException.class, () -> JsonDeserializer.toObj(SimpleIntegerValueObject.class, ""));
    }

    @Test
    public void JsonDeserializer_SetValuesOnImmutableObject_FieldValueCorrect()
    {
        SimpleImmutableObject obj = JsonDeserializer.setValuesFromJsonString(new SimpleImmutableObject("Original"),
                "{ \"Value\": \"New\" }");

        Assert.assertEquals("New", obj.Value);    }

    @Test
    public void JsonDeserializer_CaseSensitiveFields_NonMatchingFieldValueNotSet()
    {
        SimpleStringValueObject obj = JsonDeserializer.toObj(SimpleStringValueObject.class,
                "{ \"value\": \"SampleValue\" }");

        Assert.assertNotEquals("SampleValue", obj.Value);
    }

    @Test
    public void JsonDeserializer_DataBoolean_IsCorrect()
    {
        SimpleBooleanValueObject obj = JsonDeserializer.toObj(SimpleBooleanValueObject.class,
                "{ \"Value1\": true, \"Value2\": true }");

        Assert.assertEquals(true, obj.Value1);
        Assert.assertEquals(true, obj.Value2);
    }

    @Test
    public void JsonDeserializer_DataString_IsCorrect()
    {
        SimpleStringValueObject obj = JsonDeserializer.toObj(SimpleStringValueObject.class,
                "{ \"Value\": \"SampleName\" }");

        Assert.assertEquals("SampleName", obj.Value);
    }

    @Test
    public void JsonDeserializer_DataNonWrappedString_NotTreatedAsString()
    {
        SimpleStringValueObject obj = JsonDeserializer.toObj(SimpleStringValueObject.class,
                "{ \"Value\": SampleName }");

        Assert.assertNotEquals("SampleName", obj.Value);
    }

    @Test
    public void JsonDeserializer_DataInteger_IsCorrect()
    {
        SimpleIntegerValueObject obj = JsonDeserializer.toObj(SimpleIntegerValueObject.class,
                "{ \"Value1\": 79, \"Value2\": 12 }");

        Assert.assertEquals(79, obj.Value1);
        Assert.assertEquals(12, (int)obj.Value2);
    }

    @Test
    public void JsonDeserializer_DataFloat_IsCorrect()
    {
        SimpleFloatValueObject obj = JsonDeserializer.toObj(SimpleFloatValueObject.class,
                "{ \"Value1\": 1.23, \"Value2\": 4.12345 }");

        Assert.assertEquals(1.23, obj.Value1, 0.01);
        Assert.assertEquals(4.12345, obj.Value2, 0.01);
    }

    @Test
    public void JsonDeserializer_DataDouble_IsCorrect()
    {
        SimpleDoubleValueObject obj = JsonDeserializer.toObj(SimpleDoubleValueObject.class,
                "{ \"Value1\": 1.23, \"Value2\": 4.12345 }");

        Assert.assertEquals(1.23, obj.Value1, 0.01);
        Assert.assertEquals(4.12345, obj.Value2, 0.01);
    }

    @Test
    public void JsonDeserializer_DataLocalDateTime_IsCorrect()
    {
        SimpleLocalDateTimeValueObject obj = JsonDeserializer.toObj(SimpleLocalDateTimeValueObject.class,
                "{ \"Value\": \"2016-07-18T02:00\" }");

        Assert.assertEquals(LocalDateTime.of(2016, 7, 18, 2, 0), obj.Value);
    }

    @Test
    // This is an enhancement on the Json spec. It allows for more flexible output objects.
    public void JsonDeserializer_StringWrappedNumbers_DeserializedIntoNumericFields()
    {
        NumericTypesObject obj = JsonDeserializer.toObj(NumericTypesObject.class,
                "{ \"intValue\": \"79\", \"longValue\": \"412345678\", \"floatValue\": \"20.16\", \"dblValue\": \"12.23\" }");

        Assert.assertEquals(79, obj.intValue);
        Assert.assertEquals(412345678, obj.longValue);
        Assert.assertEquals(20.16, obj.floatValue, 0.01);
        Assert.assertEquals(12.23, obj.dblValue, 0.01);
    }

    @Test
    public void JsonDeserializer_DataInnerJavaObject_SubObjectValuesCorrect()
    {
        SimpleSubObjectValueObject obj = JsonDeserializer.toObj(SimpleSubObjectValueObject.class,
                "{ \"Value\": { \"Value1\": 1, \"Value2\": 3 } }");

        Assert.assertEquals(1, obj.Value.Value1);
        Assert.assertEquals(3, (int)obj.Value.Value2);
    }

    @Test
    public void JsonDeserializer_EmptyList_IsCorrect()
    {
        SimpleStringListValueObject obj = JsonDeserializer.toObj(SimpleStringListValueObject.class,
                "{ \"Value\": [] }");

        Assert.assertEquals(0, obj.Value.size());
    }

    @Test
    public void JsonDeserializer_ListOfStrings_IsCorrect()
    {
        SimpleStringListValueObject obj = JsonDeserializer.toObj(SimpleStringListValueObject.class,
                "{ \"Value\": [ \"JC Denton\", \"Adam Jensen\" ] }");

        Assert.assertEquals(2, obj.Value.size());
        Assert.assertEquals("JC Denton", obj.Value.get(0));
        Assert.assertEquals("Adam Jensen", obj.Value.get(1));
    }

    @Test
    public void JsonDeserializer_NestedListOfIntegers_IsCorrect()
    {
        NestedListObject obj = JsonDeserializer.toObj(NestedListObject.class,
                "{ \"Value\": [ [ [ [ 9, 8, 7 ] ] ] ] }");

        Assert.assertEquals(3, obj.getInnerValues().size());
        Assert.assertTrue(obj.getInnerValues().containsAll(Arrays.asList(9, 8, 7)));
    }

    @Test
    public void JsonDeserializer_ArrayEmpty_IsCorrect()
    {
        SimpleStringArrayValueObject obj = JsonDeserializer.toObj(SimpleStringArrayValueObject.class,
                "{ \"Value\": [] }");

        Assert.assertEquals(0, obj.Value.length);
    }

    @Test
    public void JsonDeserializer_ArrayString_IsCorrect()
    {
        SimpleStringArrayValueObject obj = JsonDeserializer.toObj(SimpleStringArrayValueObject.class,
                "{ \"Value\": [ \"Obi Wan Kenobi\", \"Yoda\" ] }");

        Assert.assertEquals(2, obj.Value.length);
        Assert.assertEquals("Obi Wan Kenobi", obj.Value[0]);
        Assert.assertEquals("Yoda", obj.Value[1]);
    }

    @Test
    public void JsonDeserializer_ArrayInt_IsCorrect()
    {
        IntegerArrayValueObject obj = JsonDeserializer.toObj(IntegerArrayValueObject.class,
                "{ \"Value1\": [ 3, 1, 4, 2, 5 ], \"Value2\": [ 1, 2, 3 ] }");

        Assert.assertArrayEquals(new int[] { 3, 1, 4, 2, 5 }, obj.Value1);
        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, obj.Value2);
    }

    @Test
    public void JsonDeserializer_ArrayByte_IsCorrect()
    {
        SimpleByteArrayValueObject obj = JsonDeserializer.toObj(SimpleByteArrayValueObject.class,
                "{ \"Value\": [ 0, -128, 127, 5 ] }");

        Assert.assertArrayEquals(new byte[] { 0, -128, 127, 5}, obj.Value);
    }

    @Test
    public void JsonDeserializer_ArrayDouble_IsCorrect()
    {
        SimpleDoubleArrayValueObject obj = JsonDeserializer.toObj(SimpleDoubleArrayValueObject.class,
                "{ \"Value\": [ 5.4321, 6.789 ] }");

        Assert.assertArrayEquals(new double[] { 5.4321, 6.789 }, obj.Value, 0.01);
    }

    @Test
    public void JsonDeserializer_ArrayLong_IsCorrect()
    {
        SimpleLongArrayValueObject obj = JsonDeserializer.toObj(SimpleLongArrayValueObject.class,
                "{ \"Value\": [ 123451234512345, 234562345623456 ] }");

        Assert.assertArrayEquals(new long[] { 123451234512345L, 234562345623456L }, obj.Value);
    }
}

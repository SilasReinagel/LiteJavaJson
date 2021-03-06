package io.theo.json;

import io.theo.json.testObjects.*;
import org.junit.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class JsonDeserializerTests
{
    @Test
    public void JsonDeserializer_InputNull_ThrowsException()
    {
        ExceptionAssert.assertThrows(RuntimeException.class, () -> Json.toObj(SimpleStringValueObject.class, null));
    }

    @Test
    public void JsonDeserializer_InputContainsNoElements_ThrowsException()
    {
        ExceptionAssert.assertThrows(RuntimeException.class, () -> Json.toObj(SimpleStringValueObject.class, "{ }"));
    }

    @Test
    public void JsonDeserializer_InputContainsNoElementsMatchingObject_ThrowsException()
    {
        ExceptionAssert.assertThrows(RuntimeException.class, () -> Json.toObj(SimpleStringValueObject.class, "{ \"abc\": 123 }"));
    }

    @Test
    public void JsonDeserializer_GetElementValue_IsCorrect()
    {
        double price = Json.getElementValue(Double.class, "Price",
                "{ \"Data1\": \"Something Useless\", \"Price\": 2.57 }");

        Assert.assertEquals(2.57, price, 0.01);
    }

    @Test
    public void JsonDeserializer_GetElementRawValue_IsCorrect()
    {
        String price = Json.getElementRawValue("Price",
                "{ \"Data1\": \"Something Useless\", \"Price\": 2.57 }");

        Assert.assertEquals("2.57", price);
    }

    @Test
    public void JsonDeserializer_MappingToObjWithImmutableConstructor_IsCorrect()
    {
        SimpleImmutableObject obj = Json.toObj(SimpleImmutableObject.class,
                "{ \"Value\": \"FancyReflectionMagic\" }");

        Assert.assertEquals("FancyReflectionMagic", obj.Value);
    }

    @Test
    public void JsonDeserializer_MappingCaseSensitiveFields_NonMatchingFieldValueNotSet()
    {
        GenericKeyValueObject obj = Json.toObj(GenericKeyValueObject.class,
                "{ \"Key\": \"Gunblade\", \"ExtraValue\": 12345 }");

        Assert.assertEquals("Gunblade", obj.Key);
        Assert.assertEquals(null, obj.Value);
    }

    @Test
    public void JsonDeserializer_MappingPrivateField_IsCorrect()
    {
        SimplePrivateFieldClass obj = Json.toObj(SimplePrivateFieldClass.class,
                "{ \"value\": \"SampleValue\" }");

        Assert.assertEquals("SampleValue", obj.getValue());
    }

    @Test
    public void JsonDeserializer_MappingParentClassField_IsCorrect()
    {
        ChildClass obj = Json.toObj(ChildClass.class,
                "{ \"ChildValue\": \"Kiddo\", \"ParentValue\": \"Pater Familias\" }");

        Assert.assertEquals("Kiddo", obj.ChildValue);
        Assert.assertEquals("Pater Familias", obj.ParentValue);
    }

    @Test
    public void JsonDeserializer_SingleValueBoolean_IsCorrect()
    {
        boolean obj = Json.toObj(Boolean.class,
                "true");

        Assert.assertEquals(true, obj);
    }

    @Test
    public void JsonDeserializer_SingleValueDouble_IsCorrect()
    {
        double obj = Json.toObj(Double.class,
                "78.87");

        Assert.assertEquals(78.87, obj, 0.01);
    }

    @Test
    public void JsonDeserializer_SingleValueString_IsCorrect()
    {
        String obj = Json.toObj(String.class,
                "\"John Doe\"");

        Assert.assertEquals("John Doe", obj);
    }

    @Test
    public void JsonDeserializer_SingleValueArrayToJavaArray_IsCorrect()
    {
        int[] obj = Json.toObj(int[].class,
                "[ 9, 8, 7, 6, 5 ]");

        Assert.assertArrayEquals(new int[]{9, 8, 7, 6, 5}, obj);
    }

    @Test
    public void JsonDeserializer_ToListOfStrings_IsCorrect()
    {
        List<String> obj = Json.toList(String.class,
            "[ \"Stratovarius\", \"Blind Guardian\" ]");

        Assert.assertEquals(2, obj.size());
        Assert.assertTrue(obj.contains("Stratovarius"));
        Assert.assertTrue(obj.contains("Blind Guardian"));
    }

    @Test
    public void JsonDeserializer_DataNullLiteral_IsNull()
    {
        SimpleStringValueObject obj = Json.toObj(SimpleStringValueObject.class,
                "{ \"Value\": null }");

        Assert.assertNull(obj.Value);
    }

    @Test
    public void JsonDeserializer_DataBoolean_IsCorrect()
    {
        SimpleBooleanValueObject obj = Json.toObj(SimpleBooleanValueObject.class,
                "{ \"Value1\": true, \"Value2\": true }");

        Assert.assertEquals(true, obj.Value1);
        Assert.assertEquals(true, obj.Value2);
    }

    @Test
    public void JsonDeserializer_DataString_IsCorrect()
    {
        SimpleStringValueObject obj = Json.toObj(SimpleStringValueObject.class,
                "{ \"Value\": \"SampleName\" }");

        Assert.assertEquals("SampleName", obj.Value);
    }

    @Test
    public void JsonDeserializer_DataInteger_IsCorrect()
    {
        SimpleIntegerValueObject obj = Json.toObj(SimpleIntegerValueObject.class,
                "{ \"Value1\": 79, \"Value2\": 12 }");

        Assert.assertEquals(79, obj.Value1);
        Assert.assertEquals(12, (int)obj.Value2);
    }

    @Test
    public void JsonDeserializer_DataFloat_IsCorrect()
    {
        SimpleFloatValueObject obj = Json.toObj(SimpleFloatValueObject.class,
                "{ \"Value1\": 1.23, \"Value2\": 4.12345 }");

        Assert.assertEquals(1.23, obj.Value1, 0.01);
        Assert.assertEquals(4.12345, obj.Value2, 0.01);
    }

    @Test
    public void JsonDeserializer_DataDouble_IsCorrect()
    {
        SimpleDoubleValueObject obj = Json.toObj(SimpleDoubleValueObject.class,
                "{ \"Value1\": 1.23, \"Value2\": 4.12345 }");

        Assert.assertEquals(1.23, obj.Value1, 0.01);
        Assert.assertEquals(4.12345, obj.Value2, 0.01);
    }

    @Test
    public void JsonDeserializer_DataLocalDateTime_IsCorrect()
    {
        SimpleLocalDateTimeValueObject obj = Json.toObj(SimpleLocalDateTimeValueObject.class,
                "{ \"Value\": \"2016-07-18T02:00\" }");

        Assert.assertEquals(LocalDateTime.of(2016, 7, 18, 2, 0), obj.Value);
    }

    @Test
    public void JsonDeserializer_DataInnerJavaObject_SubObjectValuesCorrect()
    {
        SimpleSubObjectValueObject obj = Json.toObj(SimpleSubObjectValueObject.class,
                "{ \"Value\": { \"Value1\": 1, \"Value2\": 3 } }");

        Assert.assertEquals(1, obj.Value.Value1);
        Assert.assertEquals(3, (int)obj.Value.Value2);
    }

    @Test
    public void JsonDeserializer_DataEnum_IsCorrect()
    {
        SimpleEnumValueObject obj = Json.toObj(SimpleEnumValueObject.class,
                "{ \"Value\": \"Value2\" }");

        Assert.assertEquals(SampleEnum.Value2, obj.Value);
    }

    @Test
    public void JsonDeserializer_DataGeneric_IsCorrect()
    {
        GenericKeyValueObject obj = Json.toObj(GenericKeyValueObject.class,
                "{ \"Key\": \"Sora's Keyblade\", \"Value\": \"Is Equipped\" }");

        Assert.assertEquals("Sora's Keyblade", obj.Key);
        Assert.assertEquals("Is Equipped", obj.Value);
    }

    @Test
    public void JsonDeserializer_DataEmptyList_IsCorrect()
    {
        SimpleStringListValueObject obj = Json.toObj(SimpleStringListValueObject.class,
                "{ \"Value\": [] }");

        Assert.assertEquals(0, obj.Value.size());
    }

    @Test
    public void JsonDeserializer_DataListOfStrings_IsCorrect()
    {
        SimpleStringListValueObject obj = Json.toObj(SimpleStringListValueObject.class,
                "{ \"Value\": [ \"JC Denton\", \"Adam Jensen\" ] }");

        Assert.assertEquals(2, obj.Value.size());
        Assert.assertEquals("JC Denton", obj.Value.get(0));
        Assert.assertEquals("Adam Jensen", obj.Value.get(1));
    }

    @Test
    public void JsonDeserializer_DataNestedListOfIntegers_IsCorrect()
    {
        NestedListObject obj = Json.toObj(NestedListObject.class,
                "{ \"Value\": [ [ [ [ 9, 8, 7 ] ] ] ] }");

        Assert.assertEquals(3, obj.getInnerValues().size());
        Assert.assertTrue(obj.getInnerValues().containsAll(Arrays.asList(9, 8, 7)));
    }

    @Test
    public void JsonDeserializer_DataListOfCustomObject_IsCorrect()
    {
        CustomObjListValueObject obj = Json.toObj(CustomObjListValueObject.class,
                "{ \"Value\": [ " +
                        "{ \"intValue\": \"79\", \"longValue\": \"412345678\", \"floatValue\": \"20.16\", \"dblValue\": \"12.23\" }, " +
                        "{ \"intValue\": \"123\", \"longValue\": \"412345678\", \"floatValue\": \"20.18\", \"dblValue\": \"7.23\" } ] }");

        Assert.assertEquals(2, obj.Value.size());
        obj.Value.forEach(x -> Assert.assertNotNull(x));
    }

    @Test
    public void JsonDeserializer_DataEmptyMap_IsCorrect()
    {
        SimpleStringMapValueObject obj = Json.toObj(SimpleStringMapValueObject.class,
                "{ \"Value\": [ ] }");

        Assert.assertEquals(0, obj.Value.size());
    }

    @Test
    public void JsonDeserializer_DataMapString_IsCorrect()
    {
        SimpleStringMapValueObject obj = Json.toObj(SimpleStringMapValueObject.class,
                "{ \"Value\": [ { \"Hello\": \"World\" } ] }");

        Assert.assertEquals(1, obj.Value.size());
        Assert.assertEquals("World", obj.Value.get("Hello"));
    }

    @Test
    public void JsonDeserializer_DataMapStringBase64ByteArray_IsCorrect()
    {
        StringByteArrayMapValueObject obj = Json.toObj(StringByteArrayMapValueObject.class,
                "{ \"Value\": [ { \"1\": \"AQID\" } ] }");

        Assert.assertEquals(1, obj.Value.size());
        Assert.assertArrayEquals(new byte[] { 1,2,3 }, obj.Value.get("1"));
    }

    @Test
    // This is an enhancement on the Json spec. It allows for more flexible output objects.
    public void JsonDeserializer_StringWrappedNumbers_DeserializedIntoNumericFields()
    {
        NumericTypesObject obj = Json.toObj(NumericTypesObject.class,
                "{ \"intValue\": \"79\", \"longValue\": \"412345678\", \"floatValue\": \"20.16\", \"dblValue\": \"12.23\" }");

        Assert.assertEquals(79, obj.intValue);
        Assert.assertEquals(412345678, obj.longValue);
        Assert.assertEquals(20.16, obj.floatValue, 0.01);
        Assert.assertEquals(12.23, obj.dblValue, 0.01);
    }

    @Test
    // This is an enhancement on the Json spec. It allows for more flexible output objects.
    public void JsonDeserializer_NonWrappedString_CanBeDeserializedToString()
    {
        SimpleStringValueObject obj = Json.toObj(SimpleStringValueObject.class,
                "{ \"Value\": SampleName }");

        Assert.assertEquals("SampleName", obj.Value);
    }

    @Test
    public void JsonDeserializer_ArrayEmpty_IsCorrect()
    {
        SimpleStringArrayValueObject obj = Json.toObj(SimpleStringArrayValueObject.class,
                "{ \"Value\": [] }");

        Assert.assertEquals(0, obj.Value.length);
    }

    @Test
    public void JsonDeserializer_ArrayString_IsCorrect()
    {
        SimpleStringArrayValueObject obj = Json.toObj(SimpleStringArrayValueObject.class,
                "{ \"Value\": [ \"Obi Wan Kenobi\", \"Yoda\" ] }");

        Assert.assertEquals(2, obj.Value.length);
        Assert.assertEquals("Obi Wan Kenobi", obj.Value[0]);
        Assert.assertEquals("Yoda", obj.Value[1]);
    }

    @Test
    public void JsonDeserializer_ArrayInt_IsCorrect()
    {
        IntegerArrayValueObject obj = Json.toObj(IntegerArrayValueObject.class,
                "{ \"Value1\": [ 3, 1, 4, 2, 5 ], \"Value2\": [ 1, 2, 3 ] }");

        Assert.assertArrayEquals(new int[] { 3, 1, 4, 2, 5 }, obj.Value1);
        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, obj.Value2);
    }

    @Test
    public void JsonDeserializer_ArrayByte_IsCorrect()
    {
        SimpleByteArrayValueObject obj = Json.toObj(SimpleByteArrayValueObject.class,
                "{ \"Value\": [ 0, -128, 127, 5 ] }");

        Assert.assertArrayEquals(new byte[] { 0, -128, 127, 5}, obj.Value);
    }

    @Test
    public void JsonDeserializer_ArrayBase64StringEncodedBytes_IsCorrect()
    {
        SimpleByteArrayValueObject obj = Json.toObj(SimpleByteArrayValueObject.class,
                "{ \"Value\": \"AQID\" }");

        Assert.assertArrayEquals(new byte[] { 1, 2, 3 }, obj.Value);
    }

    @Test
    public void JsonDeserializer_ArrayDouble_IsCorrect()
    {
        SimpleDoubleArrayValueObject obj = Json.toObj(SimpleDoubleArrayValueObject.class,
                "{ \"Value\": [ 5.4321, 6.789 ] }");

        Assert.assertArrayEquals(new double[] { 5.4321, 6.789 }, obj.Value, 0.01);
    }

    @Test
    public void JsonDeserializer_ArrayLong_IsCorrect()
    {
        SimpleLongArrayValueObject obj = Json.toObj(SimpleLongArrayValueObject.class,
                "{ \"Value\": [ 123451234512345, 234562345623456 ] }");

        Assert.assertArrayEquals(new long[] { 123451234512345L, 234562345623456L }, obj.Value);
    }
}

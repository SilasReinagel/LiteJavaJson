package io.theo.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class JsonSerializer
{
    private static final List<String> _numericTypes = Arrays.asList(
            "int", "Integer",
            "long", "Long",
            "float", "Float",
            "double", "Double");

    private static final List<String> _nonNumericTypes = Arrays.asList(
            "boolean", "Boolean",
            "String",
            "LocalDateTime");


    private JsonSerializer()
    {
    }

    public static String toJsonString(final Object obj)
    {
        String values = "";
        for (Field field : obj.getClass().getFields())
            values += ("\"" + field.getName() + "\": " + toJsonValue(getFieldValue(field, obj)) + ", ");
        return wrapCommaSeparatedValues('{', '}', values);
    }

    private static Object getFieldValue(final Field field, final Object obj)
    {
        try
        {
            return field.get(obj);
        }
        catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private static String toJsonValue(final Object obj)
    {
        if (obj == null)
            return "null";

        String objType = obj.getClass().getSimpleName();

        if (obj instanceof List<?>)
            return getListAsJsonArray(obj);
        if (obj.getClass().isArray())
            return toJsonArray(obj);
        if (obj.getClass().isEnum())
            return "\"" + obj.toString() + "\"";
        if (isCustomObjectType(objType))
            return toJsonString(obj);
        if (isNumericType(objType) || isBooleanType(objType))
            return obj.toString();
        return "\"" + obj.toString() + "\"";
    }

    private static boolean isBooleanType(final String fieldType)
    {
        return fieldType.equals("boolean") || fieldType.equals("Boolean");
    }

    private static boolean isNumericType(final String fieldType)
    {
        return _numericTypes.contains(fieldType);
    }

    private static boolean isCustomObjectType(String fieldType)
    {
        return !(_numericTypes.contains(fieldType) || _nonNumericTypes.contains(fieldType));
    }

    private static String toJsonArray(final Object array)
    {
        String values = "";
        for (int i = 0; i < Array.getLength(array); i ++)
            values += toJsonValue(Array.get(array, i)) + ", ";
        return wrapCommaSeparatedValues('[', ']', values);
    }

    private static String getListAsJsonArray(final Object list)
    {
        String values = "";
        for (Object item : (List<?>) list)
            values += toJsonValue(item) + ", ";
        return wrapCommaSeparatedValues('[', ']', values);
    }

    private static String wrapCommaSeparatedValues(final char opening, final char closing, final String values)
    {
        return !values.contains(",")
                ? opening + " " + closing
                : opening + " " + values.substring(0, values.lastIndexOf(",")) + " " + closing;
    }
}

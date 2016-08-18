package io.theo.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public final class JsonSerializer
{
    private static final List<String> _numericTypes = Arrays.asList(
            "byte", "Byte",
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
        return toJsonValue(obj);
    }

    private static boolean isJsonString(final Object obj)
    {
        if (!(obj instanceof String))
            return false;
        String str = ((String)obj).trim();
        return str.startsWith("{") && str.endsWith("}");
    }

    private static List<Field> getFields(final Object obj)
    {
        Set<Field> fields = new LinkedHashSet<>();
        Arrays.stream(obj.getClass().getFields())
                .forEach(x -> fields.add(x));
        Arrays.stream(obj.getClass().getDeclaredFields())
            .filter(x -> Modifier.isPrivate(x.getModifiers()) && !Modifier.isStatic(x.getModifiers()))
            .forEach(x -> fields.add(x));
        return fields.stream().collect(Collectors.toList());
    }

    private static Object getFieldValue(final Field field, final Object obj)
    {
        try
        {
            if (!field.isAccessible())
                field.setAccessible(true);
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
        if (isJsonString(obj))
            return (String)obj;

        String objType = obj.getClass().getSimpleName();

        if (obj instanceof String || obj.getClass().isEnum())
            return "\"" + obj.toString() + "\"";
        if (obj instanceof List<?>)
            return getListAsJsonArray(obj);
        if (obj.getClass().isArray())
            return toJsonArray(obj);
        if (Map.class.isAssignableFrom(obj.getClass()))
            return getMapAsObj(obj);
        if (isNumericType(objType) || isBooleanType(objType))
            return obj.toString();
        if (isCustomObjectType(objType))
            return toJsonObject(obj);
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

    private static String toJsonObject(final Object obj)
    {
        String values = "";
        for (Field field : getFields(obj))
            values += ("\"" + field.getName() + "\": " + toJsonValue(getFieldValue(field, obj)) + ", ");
        return wrapCommaSeparatedValues('{', '}', values);
    }

    private static String getMapAsObj(final Object obj)
    {
        String values = "";
        for (Object entry : ((Map)obj).entrySet())
            values += "{ \"" + ((Map.Entry)entry).getKey() + "\": " + toJsonValue(((Map.Entry)entry).getValue()) + " }, ";
        return wrapCommaSeparatedValues('[', ']', values);
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

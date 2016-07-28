package io.theo.json;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public final class JsonDeserializer
{
    private JsonDeserializer() { }

    public static boolean isJsonObject(final String input)
    {
        return isWrappedWith("{", "}", input);
    }

    public static <T> T toObj(final Class<T> type, final String jsonString)
    {
        return setValuesFromJsonString(createNewInstance(type), jsonString);
    }

    public static <T> T getElementValue(final Class<T> type, final String elementName, final String jsonString)
    {
        return (T) getObjectValue(type, getJsonElements(jsonString).get(elementName));
    }

    public static String getElementRawValue(final String elementName, final String jsonString)
    {
        return getJsonElements(jsonString).get(elementName);
    }

    private static <T> T setValuesFromJsonString(final T obj, final String jsonString)
    {
        if (!isJsonObject(jsonString))
            throw new RuntimeException("Invalid object string: " + obj);

        Map<String, String> jsonElements = getJsonElements(jsonString);
        for (Field field : obj.getClass().getFields())
            if (jsonElements.containsKey(field.getName()))
                setFieldValue(obj, field, jsonElements.get(field.getName()));
        return obj;
    }

    private static Map<String, String> getJsonElements(final String jsonString)
    {
        return getElementStrings(unwrap(jsonString)).stream().collect(Collectors.toMap(x -> getElementKey(x), x -> getElementValue(x)));
    }

    private static String getElementKey(final String input)
    {
        return getJsonStringValue(input.substring(0, input.indexOf(":")).trim());
    }

    private static String getElementValue(final String input)
    {
        return input.substring(input.indexOf(":") + 1).trim();
    }

    private static List<String> getElementStrings(final String input)
    {
        List<String> values = new ArrayList<>();

        if (input.length() == 0)
            return values;

        boolean foundKey = false;
        int currentDepth = 0;
        String currentValue = "";
        for (int i = 0; i < input.length(); i++)
        {
            char ch = input.charAt(i);
            if (currentDepth != 0 || ch != ',')
                currentValue += ch;
            if (!foundKey && ch == ':')
                foundKey = true;
            if (foundKey && isSubObjectOpener(ch))
                currentDepth++;
            if (currentDepth != 0 && isSubObjectCloser(ch))
                currentDepth--;
            if (foundKey && currentDepth == 0 && isTerminator(ch))
            {
                values.add(currentValue);
                currentValue = "";
                foundKey = false;
            }
        }

        if (currentValue.length() > 0)
            values.add(currentValue);
        return values;
    }

    private static boolean isSubObjectOpener(final char ch)
    {
        return ch == '{' || ch == '[';
    }

    private static boolean isSubObjectCloser(final char ch)
    {
        return ch == '}' || ch == ']';
    }

    private static boolean isTerminator(final char ch)
    {
        return ch == ',' || isSubObjectCloser(ch);
    }

    // Field is required because of Generic Type Erasure
    private static Object getObjectValue(final Field field, final String stringValue)
    {
        if (isJsonArray(stringValue))
            return toCollection(field, stringValue);
        return getObjectValue(field.getType(), stringValue);
    }

    private static Object getObjectValue(final Class type, final String stringValue)
    {
        String fieldType = type.getSimpleName();
        if (fieldType.equals("String"))
            return getJsonStringValue(stringValue);
        if (isJsonObject(stringValue))
            return toObj(type.getCanonicalName(), stringValue);

        String extractedValue = stringValue.replace("\"", "");
        if (fieldType.equals("byte") || fieldType.equals("Byte"))
            return Byte.parseByte(extractedValue);
        if (fieldType.equals("boolean") || fieldType.equals("Boolean"))
            return Boolean.parseBoolean(extractedValue);
        if (fieldType.equals("int") || fieldType.equals("Integer"))
            return Integer.parseInt(extractedValue);
        if (fieldType.equals("long") || fieldType.equals("Long"))
            return Long.parseLong(extractedValue);
        if (fieldType.equals("float") || fieldType.equals("Float"))
            return Float.parseFloat(extractedValue);
        if (fieldType.equals("double") || fieldType.equals("Double"))
            return Double.parseDouble(extractedValue);
        if (type.isEnum())
            return getEnumValue(type, extractedValue);
        if (fieldType.equals("LocalDateTime"))
            return LocalDateTime.parse(extractedValue);

        return null;
    }

    private static Object getEnumValue(final Class type, final String stringValue)
    {
        return Enum.valueOf(type, stringValue);
    }

    private static String getJsonStringValue(final String input)
    {
        return isJsonString(input) ? unwrap(input) : null;
    }

    private static boolean isJsonString(final String input)
    {
        return isWrappedWith("\"", "\"", input);
    }

    private static Object toCollection(final Field field, final String jsonArray)
    {
        if (field.getType().getSimpleName().equals("List"))
            return toList((ParameterizedType)field.getGenericType(), jsonArray);
        if (field.getType().isArray())
            return toArray(field.getType(), jsonArray);
        return null;
    }

    private static <T> T createNewInstance(final Class<T> type)
    {
        try
        {
            ReflectionFactory factory = ReflectionFactory.getReflectionFactory();
            Constructor constructor = factory.newConstructorForSerialization(type, Object.class.getDeclaredConstructor());
            return type.cast(constructor.newInstance());
            //return type.newInstance();
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static <T> T toObj(final String fullyQualifiedTypeName, final String jsonString)
    {
        try
        {
            return (T) toObj(Class.forName(fullyQualifiedTypeName), jsonString);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void setFieldValue(final Object obj, final Field field, final String stringValue)
    {
        Object value;
        try
        {
            value = getObjectValue(field, stringValue);
        }
        catch (Exception e)
        {
            return;
        }

        setFieldValue(obj, field, value);
    }

    private static void setFieldValue(final Object obj, final Field field, final Object value)
    {
        try
        {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(accessible);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static boolean isWrappedWith(final String opener, final String closer, final String input)
    {
        String trimmed = input.trim();
        return trimmed.startsWith(opener) && trimmed.endsWith(closer);
    }

    private static String unwrap(final String obj)
    {
        String trimmed = obj.trim();
        return trimmed.substring(1, trimmed.length() - 1).trim();
    }

    private static boolean isJsonArray(final String jsonElement)
    {
        return isWrappedWith("[", "]", jsonElement);
    }

    private static Class getArrayItemType(final Class type)
    {
        return type.isArray() ? type.getComponentType() : type;
    }

    private static List toList(final ParameterizedType type, final String jsonArray)
    {
        Type innerType = type.getActualTypeArguments()[0];
        if (innerType instanceof ParameterizedType)
            return Collections.singletonList(toList((ParameterizedType)innerType, unwrap(jsonArray)));
        if (innerType instanceof Class)
            return toList((Class)innerType, jsonArray);
        return new ArrayList<>();
    }

    private static List toList(final Class type, final String jsonArray)
    {
        return Arrays.stream(unwrap(jsonArray).split("\\s*,\\s*"))
                .filter(x -> x.trim().length() > 0)
                .map(x -> getObjectValue(getArrayItemType(type), x.trim()))
                .collect(Collectors.toList());
    }

    private static Object toArray(final Class type, final String jsonArray)
    {
        List values = toList(type, jsonArray);

        final Class itemType = getArrayItemType(type);
        int count = values.size();

        if (itemType.getSimpleName().equals("int"))
        {
            int[] result = new int[count];
            for (int i = 0; i < count; i++)
                result[i] = (int) values.get(i);
            return result;
        }

        if (itemType.getSimpleName().equals("long"))
        {
            long[] result = new long[count];
            for (int i = 0; i < count; i++)
                result[i] = (long) values.get(i);
            return result;
        }

        if (itemType.getSimpleName().equals("float"))
        {
            float[] result = new float[count];
            for (int i = 0; i < count; i++)
                result[i] = (float) values.get(i);
            return result;
        }

        if (itemType.getSimpleName().equals("double"))
        {
            double[] result = new double[count];
            for (int i = 0; i < count; i++)
                result[i] = (double) values.get(i);
            return result;
        }

        if (itemType.getSimpleName().equals("byte"))
        {
            byte[] result = new byte[count];
            for (int i = 0; i < count; i++)
                result[i] = (byte) values.get(i);
            return result;
        }

        return values.toArray((Object[]) Array.newInstance(itemType, count));
    }
}

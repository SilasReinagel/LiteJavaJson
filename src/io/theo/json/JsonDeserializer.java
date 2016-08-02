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

    @SuppressWarnings("unchecked")
    public static <T> T toObj(final Class<T> type, final String jsonString)
    {
        return (T) getObjectValue(type, jsonString);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(final Class<T> itemType, final String jsonString)
    {
        return toObjectList(itemType, jsonString);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getElementValue(final Class<T> type, final String elementName, final String jsonString)
    {
        return (T) getObjectValue(type, getJsonElements(jsonString).get(elementName));
    }

    public static String getElementRawValue(final String elementName, final String jsonString)
    {
        return getJsonElements(jsonString).get(elementName);
    }

    private static <T> T getJsonObj(final Class<T> type, final String jsonString)
    {
        return setValuesFromJsonString(createNewInstance(type), jsonString);
    }

    private static <T> T setValuesFromJsonString(final T obj, final String jsonString)
    {
        Map<String, String> jsonElements = getJsonElements(jsonString);
        if (jsonElements.size() == 0)
            throw new RuntimeException("Json string contains no elements.");

        List<Field> fieldsToMap = getFields(obj).stream()
                .filter(x -> jsonElements.containsKey(x.getName()))
                .collect(Collectors.toList());
        if (fieldsToMap.size() == 0)
            throw new RuntimeException("No Json string elements match object type: " + obj.getClass());

        fieldsToMap.forEach(x -> setFieldValue(obj, x, jsonElements.get(x.getName())));
        return obj;
    }

    private static void validateJsonString(final String jsonString)
    {
        if (jsonString == null)
            throw new RuntimeException("Invalid Json string: null");
        if (!isJsonObject(jsonString))
            throw new RuntimeException("Invalid Json string: " + jsonString);
    }

    private static List<Field> getFields(Object obj)
    {
        Set<Field> fields = new LinkedHashSet<>();
        Arrays.stream(obj.getClass().getFields())
                .forEach(x -> fields.add(x));
        Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(x -> Modifier.isPrivate(x.getModifiers()) && !Modifier.isStatic(x.getModifiers()))
                .forEach(x -> fields.add(x));
        return fields.stream().collect(Collectors.toList());
    }

    private static Map<String, String> getJsonElements(final String jsonString)
    {
        validateJsonString(jsonString);
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
            if (foundKey && isObjectOpener(ch))
                currentDepth++;
            if (currentDepth != 0 && isObjectCloser(ch))
                currentDepth--;
            if (foundKey && currentDepth == 0 && isTerminator(ch))
            {
                values.add(currentValue);
                currentValue = "";
                foundKey = false;
            }
        }

        if (currentValue.trim().length() > 0)
            values.add(currentValue);
        return values;
    }

    private static boolean isObjectOpener(final char ch)
    {
        return ch == '{' || ch == '[';
    }

    private static boolean isObjectCloser(final char ch)
    {
        return ch == '}' || ch == ']';
    }

    private static boolean isTerminator(final char ch)
    {
        return ch == ',' || isObjectCloser(ch);
    }

    // Field is required for Lists because of Generic Type Erasure
    private static Object getObjectValue(final Field field, final String stringValue)
    {
        if (isJsonArray(stringValue) && field.getType().getSimpleName().equals("List"))
            return toObjectList((ParameterizedType)field.getGenericType(), stringValue);
        return getObjectValue(field.getType(), stringValue);
    }

    private static Object getObjectValue(final Class type, final String stringValue)
    {
        String fieldType = type.getSimpleName();
        if (isJsonObject(stringValue))
            return getJsonObj(type.getCanonicalName(), stringValue);
        if (isJsonArray(stringValue) && type.isArray())
            return toArray(type, stringValue);
        if (fieldType.equals("String"))
            return getJsonStringValue(stringValue);

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
        if (fieldType.equals("Object"))
            return extractedValue;

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

    private static <T> T createNewInstance(final Class<T> type)
    {
        try
        {
            ReflectionFactory factory = ReflectionFactory.getReflectionFactory();
            Constructor constructor = factory.newConstructorForSerialization(type, Object.class.getDeclaredConstructor());
            return type.cast(constructor.newInstance());
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static <T> T getJsonObj(final String fullyQualifiedTypeName, final String jsonString)
    {
        try
        {
            return (T) getJsonObj(Class.forName(fullyQualifiedTypeName), jsonString);
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

    private static List toObjectList(final ParameterizedType type, final String jsonArray)
    {
        Type innerType = type.getActualTypeArguments()[0];
        if (innerType instanceof ParameterizedType)
            return Collections.singletonList(toObjectList((ParameterizedType)innerType, unwrap(jsonArray)));
        if (innerType instanceof Class)
            return toObjectList((Class)innerType, jsonArray);
        return new ArrayList<>();
    }

    private static <T> List<T> toObjectList(final Class<T> itemType, final String jsonArray)
    {
        return getStringValuesFromJsonArray(jsonArray).stream()
                .map(x -> (T)getObjectValue(getArrayItemType(itemType), x.trim()))
                .collect(Collectors.toList());
    }

    private static List<String> getStringValuesFromJsonArray(final String jsonArray)
    {
        List<String> valueStrings = new ArrayList<>();

        int startIndex = -1;
        int endIndex = -1;
        int objectDepth = 0;
        for (int i = 0; i < jsonArray.length(); i++)
        {
            char ch = jsonArray.charAt(i);
            if (Character.isWhitespace(ch))
                continue;
            if (isObjectCloser(ch))
                objectDepth--;
            if (startIndex == -1 && objectDepth > 0)
                startIndex = i;
            if (startIndex != -1 && objectDepth > 0)
                endIndex = i + 1;
            if (isObjectOpener(ch))
                objectDepth++;
            if (objectDepth == 1 && ch == ',')
                endIndex = endIndex - 1;
            if (objectDepth == 0 || objectDepth == 1 && ch == ',')
            {
                if (startIndex > 0 && endIndex > startIndex)
                    valueStrings.add(jsonArray.substring(startIndex, endIndex));
                startIndex = -1;
            }
        }

        return valueStrings;
    }

    private static Object toArray(final Class type, final String jsonArray)
    {
        List values = toObjectList(type, jsonArray);

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

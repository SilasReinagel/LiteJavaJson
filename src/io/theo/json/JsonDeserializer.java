package io.theo.json;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class JsonDeserializer
{
    private static Map<Class, Constructor> _constructors = new HashMap<>();
    private static Map<Field, ParameterizedType> _genericTypes = new HashMap<>();
    private static Map<Class, List<Field>> _classFields = new HashMap<>();
    private static Map<Class, String> _canonicalNameByClass = new HashMap<>();
    private static Map<String, Class> _classByCanonicalName = new HashMap<>();

    private static Map<Class, Function<String, Object>> _parsers = new HashMap<Class, Function<String, Object>>()
    {{
        put(byte.class, x -> Byte.parseByte(x));
        put(Byte.class, x -> Byte.parseByte(x));
        put(boolean.class, x -> Boolean.parseBoolean(x));
        put(Boolean.class, x -> Boolean.parseBoolean(x));
        put(int.class, x -> Integer.parseInt(x));
        put(Integer.class, x -> Integer.parseInt(x));
        put(long.class, x -> Long.parseLong(x));
        put(Long.class, x -> Long.parseLong(x));
        put(float.class, x -> Float.parseFloat(x));
        put(Float.class, x -> Float.parseFloat(x));
        put(double.class, x -> Double.parseDouble(x));
        put(Double.class, x -> Double.parseDouble(x));
        put(LocalDateTime.class, x -> LocalDateTime.parse(x));
        put(String.class, x -> getUnwrappedString(x));
        put(Object.class, x -> x);
    }};

    private JsonDeserializer() { }

    public static boolean isJsonObject(final String input)
    {
        return isWrappedWith("{", "}", input);
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObj(final Class<T> type, final String jsonString)
    {
        return (T)getObjectValue(type, jsonString);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(final Class<T> itemType, final String jsonString)
    {
        return toObjectList(itemType, jsonString);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getElementValue(final Class<T> type, final String elementName, final String jsonString)
    {
        return (T)getObjectValue(type, getJsonElements(jsonString).get(elementName));
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

    private static List<Field> getFields(final Object obj)
    {
        Class objClass = obj.getClass();
        if (!_classFields.containsKey(objClass))
            cacheClassFields(obj);
        return _classFields.get(objClass);
    }

    private static void cacheClassFields(final Object obj)
    {
        Set<Field> fields = new LinkedHashSet<>();
        Arrays.stream(obj.getClass().getFields())
                .forEach(x -> fields.add(x));
        Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(x -> Modifier.isPrivate(x.getModifiers()) && !Modifier.isStatic(x.getModifiers()))
                .forEach(x -> fields.add(x));
        List<Field> fieldsList = fields.stream().collect(Collectors.toList());
        _classFields.put(obj.getClass(), fieldsList);
    }

    private static Map<String, String> getJsonElements(final String jsonString)
    {
        validateJsonString(jsonString);
        return getElements(jsonString).stream().collect(Collectors.toMap(x -> getUnwrappedElementKey(x), x -> getElementValue(x)));
    }

    private static String getUnwrappedElementKey(final String input)
    {
        return getJsonStringValue(getElementKey(input));
    }

    private static String getElementKey(final String input)
    {
        return input.substring(0, input.indexOf(":")).trim();
    }

    private static String getElementValue(final String input)
    {
        return input.substring(input.indexOf(":") + 1).trim();
    }

    private static List<String> getElements(final String input)
    {
        List<String> elements = new ArrayList<>();

        int startIndex = -1;
        int endIndex = -1;
        int objectDepth = 0;
        for (int i = 0; i < input.length(); i++)
        {
            char ch = input.charAt(i);
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
                    elements.add(input.substring(startIndex, endIndex));
                startIndex = -1;
            }
        }

        return elements;
    }

    private static boolean isObjectOpener(final char ch)
    {
        return ch == '{' || ch == '[';
    }

    private static boolean isObjectCloser(final char ch)
    {
        return ch == '}' || ch == ']';
    }

    // Field is required for Lists and Maps because of Generic Type Erasure
    private static Object getObjectValue(final Field field, final String stringValue)
    {
        if (List.class.isAssignableFrom(field.getType()) && isJsonArray(stringValue))
            return toObjectList(getParameterizedType(field), stringValue);
        if (Map.class.isAssignableFrom(field.getType()) && isJsonArray(stringValue))
            return toObjectMap(getParameterizedType(field), stringValue);
        return getObjectValue(field.getType(), stringValue);
    }

    private static Object getObjectValue(final Class type, final String stringValue)
    {
        String unwrapped = getUnwrappedString(stringValue);
        if (type.isEnum())
            return Enum.valueOf(type, unwrapped);
        if (type.isArray() && isJsonArray(stringValue))
            return toArray(type, stringValue);
        if (type.equals(byte[].class) && isJsonString(stringValue))
            return getBase64Bytes(unwrapped);
        if (isJsonObject(stringValue))
            return getJsonObj(getCanonicalName(type), stringValue);
        if (_parsers.containsKey(type))
            return _parsers.get(type).apply(unwrapped);
        return null;
    }

    private static Object getBase64Bytes(final String stringValue)
    {
        return Base64.getDecoder().decode(stringValue);
    }

    private static String getJsonStringValue(final String input)
    {
        return isJsonString(input) ? unwrap(input) : null;
    }

    private static String getUnwrappedString(final String input)
    {
        return isJsonString(input) ? unwrap(input) : input;
    }

    private static boolean isJsonString(final String input)
    {
        return isWrappedWith("\"", "\"", input);
    }

    private static String getCanonicalName(final Class type)
    {
        if(!_canonicalNameByClass.containsKey(type))
            _canonicalNameByClass.put(type, type.getCanonicalName());
        return _canonicalNameByClass.get(type);
    }

    private static ParameterizedType getParameterizedType(final Field field)
    {
        if (!_genericTypes.containsKey(field))
            _genericTypes.put(field, (ParameterizedType)field.getGenericType());
        return _genericTypes.get(field);
    }

    private static <T> T createNewInstance(final Class<T> type)
    {
        try
        {
            if (!_constructors.containsKey(type))
                _constructors.put(type, ReflectionFactory.getReflectionFactory().newConstructorForSerialization(type, Object.class.getDeclaredConstructor()));
            return type.cast(_constructors.get(type).newInstance());
        }
        catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getJsonObj(final String canonicalName, final String jsonString)
    {
        try
        {
            if (!_classByCanonicalName.containsKey(canonicalName))
                _classByCanonicalName.put(canonicalName, Class.forName(canonicalName));
            return (T) getJsonObj(_classByCanonicalName.get(canonicalName), jsonString);
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

    private static Object toObjectMap(final ParameterizedType genericType, final String jsonArray)
    {
        Type keyType = genericType.getActualTypeArguments()[0];
        Type valueType = genericType.getActualTypeArguments()[1];
        Map<String, String> itemStrings = getRawJsonElements(unwrap(jsonArray));
        Map itemMap = new HashMap<>();
        itemStrings.entrySet().forEach(x -> putItem(x, itemMap, (Class)keyType, (Class)valueType));
        return itemMap;
    }

    private static Map<String, String> getRawJsonElements(final String jsonString)
    {
        return getElements(jsonString).stream().collect(Collectors.toMap(x -> getElementKey(x), x -> getElementValue(x)));
    }

    private static void putItem(final Map.Entry<String, String> item, Map map, final Class keyType, final Class valueType)
    {
        Object key = toObj(keyType, item.getKey());
        Object value = toObj(valueType, item.getValue());
        map.put(key, value);
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

    private static List toObjectList(final Class type, final String jsonArray)
    {
        Class itemType = getArrayItemType(type);
        return getElements(jsonArray).stream()
                .map(x -> getObjectValue(itemType, x))
                .collect(Collectors.toList());
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

package io.theo.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class JsonSerializer
{
    private static final BiConsumer<StringBuilder, Object> _writeLiteral = (sb, x) -> sb.append(x.toString());
    private static final BiConsumer<StringBuilder, Object> _writeString = (sb, x) -> sb.append("\"").append(x.toString()).append("\"");

    private static final Map<Class, BiConsumer<StringBuilder, Object>> _writers = new HashMap<Class, BiConsumer<StringBuilder, Object>>()
    {{
        put(byte.class, _writeLiteral);
        put(Byte.class, _writeLiteral);
        put(int.class, _writeLiteral);
        put(Integer.class, _writeLiteral);
        put(long.class, _writeLiteral);
        put(Long.class, _writeLiteral);
        put(float.class, _writeLiteral);
        put(Float.class, _writeLiteral);
        put(double.class, _writeLiteral);
        put(Double.class, _writeLiteral);
        put(boolean.class, _writeLiteral);
        put(Boolean.class, _writeLiteral);
        put(String.class, _writeString);
        put(LocalDateTime.class, _writeString);
        put(byte[].class, (sb, x) -> writeJsonValue(sb, Base64.getEncoder().encodeToString((byte[])x)));
    }};

    private static final Map<Class, List<Field>> _classFields = new HashMap<>();

    private JsonSerializer()
    {
    }

    public static String toJsonString(final Object obj)
    {
        return writeJsonValue(new StringBuilder(), obj).toString();
    }

    private static StringBuilder writeJsonValue(final StringBuilder sb, final Object obj)
    {
        getWriter(obj).accept(sb, obj);
        return sb;
    }

    private static BiConsumer<StringBuilder, Object> getWriter(final Object obj)
    {
        if (obj == null)
            return (sb, x) -> sb.append("null");

        Class objClass = obj.getClass();
        if (!_writers.containsKey(objClass))
            initWriter(obj);
        return (sb, x) -> _writers.get(objClass).accept(sb, x);
    }

    private static void initWriter(final Object obj)
    {
        Class objClass = obj.getClass();
        if (objClass.isEnum())
            _writers.put(objClass, _writeString);
        if (obj instanceof List<?>)
            _writers.put(objClass, (sb, x) -> writeList(sb, x));
        if (Map.class.isAssignableFrom(obj.getClass()))
            _writers.put(objClass, (sb, x) -> writeMap(sb, x));
        if (objClass.isArray())
            _writers.put(objClass, (sb, x) -> writeArray(sb, x));
        if (!_writers.containsKey(objClass))
            _writers.put(objClass, (sb, x) -> writeJsonObj(sb, x));
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

    private static void writeJsonObj(final StringBuilder sb, final Object obj)
    {
        sb.append("{ ");
        for (Field field : getFields(obj))
        {
            sb.append("\"").append(field.getName()).append("\": ");
            writeJsonValue(sb, getFieldValue(field, obj)).append(", ");
        }
        removeExtraComma(sb).append("}");
    }

    private static void writeList(final StringBuilder sb, final Object list)
    {
        writeJsonArray(sb, () -> {
            for (Object item : (List<?>) list)
                writeJsonValue(sb, item).append(", "); });
    }

    private static void writeArray(final StringBuilder sb, final Object array)
    {
        writeJsonArray(sb, () -> {
            for (int i = 0; i < Array.getLength(array); i ++)
                writeJsonValue(sb, Array.get(array, i)).append(", ");});
    }

    private static void writeMap(final StringBuilder sb, final Object map)
    {
        writeJsonArray(sb, () -> {
            for (Object entry : ((Map)map).entrySet())
            {
                sb.append("{ \"").append(((Map.Entry)entry).getKey()).append("\": ");
                writeJsonValue(sb, ((Map.Entry)entry).getValue()).append(" }, "); }
            });
    }

    private static void writeJsonArray(final StringBuilder sb, final Runnable runnable)
    {
        sb.append("[ ");
        runnable.run();
        removeExtraComma(sb).append("]");
    }

    private static StringBuilder removeExtraComma(final StringBuilder sb)
    {
        int lastCommaIndex = sb.lastIndexOf(",");
        return lastCommaIndex != sb.length() - 2 ? sb : sb.deleteCharAt(lastCommaIndex);
    }
}

package io.theo.json.testObjects;

import org.junit.Assert;

import java.lang.reflect.Type;

public class ExceptionAssert
{
    private ExceptionAssert()
    {
    }

    public static void assertThrows(Type exceptionType, Runnable runnable)
    {
        try
        {
            runnable.run();
            Assert.fail(String.format("Expected exception of type <%s>, but no exception was thrown.", exceptionType.getTypeName()));
        }
        catch (Exception ex)
        {
            if (!(ex.getClass() == exceptionType) && !(ex.getClass().getSuperclass() == exceptionType))
                Assert.fail(String.format("Expected exception type: <%s>. Actual: <%s>", exceptionType.getTypeName(), ex.getClass().getTypeName()));
        }
    }

    public static <T> T getException(Type exceptionType, Runnable runnable)
    {
        try
        {
            runnable.run();
            Assert.fail(String.format("Expected exception of type <%s>, but no exception was thrown.", exceptionType.getTypeName()));
            throw new Exception("No exception to return");
        }
        catch (Exception ex)
        {
            if (!(ex.getClass() == exceptionType) && !(ex.getClass().getSuperclass() == exceptionType))
                Assert.fail(String.format("Expected exception type: <%s>. Actual: <%s>", exceptionType.getTypeName(), ex.getClass().getTypeName()));
            return (T)ex;
        }
    }
}
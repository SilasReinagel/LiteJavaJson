package io.theo.json.testObjects;

import java.time.LocalDateTime;

public class SimpleLocalDateTimeValueObject
{
    public LocalDateTime Value;

    public SimpleLocalDateTimeValueObject()
    {
    }

    public SimpleLocalDateTimeValueObject(final LocalDateTime dateTime)
    {
        Value = dateTime;
    }
}

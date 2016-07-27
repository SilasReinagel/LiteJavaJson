# LiteJavaJson

The purpose of this project is to provide a very simple, no-fluff, single source file Java JSON serializer and JSON deserializer.

This project is designed to offer maximum value with minimal setup/configuration/learning. 

## Usage

### Serialization

```Java
// Sample class
public class SimpleStringValueObject
{
    public String Value;
}

SimpleStringValueObject obj = new SimpleStringValueObject();
obj.Value = "Important Data Here";
String jsonString = JsonSerializer.toJsonString(obj); 
// jsonString: { "Value": "Important Data Here" }
```

### Deserialization

```Java
SimpleStringValueObject obj = JsonDeserializer.toObj(SimpleStringValueObject.class, "{ \"Value\": \"Important Data Here\" }");
```

### Get Element Value (no Java object required)


```Java
double price = JsonDeserializer.getElementValue(Double.class, "Price","{ \"Data1\": \"Something Useless\", \"Price\": 2.57 }");
```

## Credits

Silas Reinagel

## License

You may use this code in part or in full however you wish. No credit or attachments are required.
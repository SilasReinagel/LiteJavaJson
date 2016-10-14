# LiteJavaJson

[![Build Status]()https://travis-ci.org/SilasReinagel/LiteJavaJson.svg?branch=master)](https://travis-ci.org/SilasReinagel/LiteJavaJson)
[![Code Quality](https://api.codacy.com/project/badge/Grade/a3fd5aa0f9d04e83a98981955e03c684)](https://www.codacy.com/app/silas-reinagel/LiteJavaJson?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TheoConfidor/LiteJavaJson&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE)

The purpose of this project is to provide a very simple, no-fluff, single source file Java JSON Serializer/Deserializer. 

This project is designed to offer maximum value with minimal setup/configuration/learning. It is designed to make Object mapping simple and painless.

The code is written in a procedural Utility-class style. 

## Usage

#### Add to Project

This library contains a single source file: `Json.java`

Copy-paste this file directly into your project.

#### Serialization

```Java
// Sample class
public class SimpleStringValueObject
{
    public String Value;
}

SimpleStringValueObject obj = new SimpleStringValueObject();
obj.Value = "Important Data Here";
String jsonString = Json.toJsonString(obj); 
// jsonString: { "Value": "Important Data Here" }
```

#### Deserialization

```Java
// Sample class
public class SimpleStringValueObject
{
    public String Value;
}

SimpleStringValueObject obj = Json.toObj(
	    SimpleStringValueObject.class, 
	    "{ \"Value\": \"Important Data Here\" }");
```

#### Deserialize Single Element Value (no Java object required)

```Java
double price = Json.getElementValue(
        Double.class, 
        "Price", 
		"{ \"Data1\": \"Something Useless\", \"Price\": 2.57 }");
```

## Credits

Silas Reinagel

## License

You may use this code in part or in full however you wish.  
No credit or attachments are required.
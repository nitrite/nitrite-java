# Nitrite Jackson Mapper

A Jackson based `NitriteMapper` for Nitrite database. No need to write separate `EntityConverter` for your entity 
classes for conversion between entity and document. This mapper uses Jackson to convert your entity classes to and 
from `Document` and `Object`.

### How To Install

To use this add required dependencies:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>nitrite-jackson-mapper</artifactId>
    </dependency>
</dependencies>
```


**Gradle**

```groovy    
implementation 'org.dizitart:nitrite-jackson-mapper'

```

### Initialize Jackson Module

```java
Nitrite db = Nitrite.builder()
        .loadModule(storeModule)
        .loadModule(new JacksonMapperModule())
        .openOrCreate("user", "password");
```

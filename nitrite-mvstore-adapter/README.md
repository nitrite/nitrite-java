# Nitrite MVStore Storage Adapter

A MVSore based `NitriteStore` implementation for Nitrite database. This storage adapter uses MVStore as the underlying storage engine.

### How To Install

To use this add required dependencies:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>nitrite-mvstore-adapter</artifactId>
    </dependency>
</dependencies>
```


**Gradle**

```groovy    
implementation 'org.dizitart:nitrite-mvstore-adapter'

```

### Initialize MVStore Module

```java
MVStoreModule storeModule = MVStoreModule.withConfig()
    .filePath(filePath)
    .compress(true)
    .build();

Nitrite db = Nitrite.builder()
    .loadModule(storeModule)
    .openOrCreate(user, password);
```

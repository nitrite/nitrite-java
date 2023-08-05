# Nitrite RocksDB Storage Adapter

A RocksDB based `NitriteStore` implementation for Nitrite database. This storage adapter uses RocksDB as the underlying storage engine.

### How To Install

To use this add required dependencies:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>nitrite-rocksdb-adapter</artifactId>
    </dependency>
</dependencies>
```


**Gradle**

```groovy    
implementation 'org.dizitart:nitrite-rocksdb-adapter'

```

### Initialize RocksDB Module

```java
RocksDBModule storeModule = RocksDBModule.withConfig()
    .filePath(filePath)
    .build();

Nitrite db = Nitrite.builder()
    .loadModule(storeModule)
    .openOrCreate(user, password);
```

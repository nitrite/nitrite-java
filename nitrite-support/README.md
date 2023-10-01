# Nitrite Support

A support library for Nitrite database to import/export data as a json file.

## How To Install

To use this add required dependencies:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>nitrite-support</artifactId>
    </dependency>
</dependencies>

```

**Gradle**

```groovy
implementation 'org.dizitart:nitrite-support'

```

## How To Use

### Export Data

```java
// export data to a json file
ExportOptions exportOptions = new ExportOptions();
exportOptions.setNitriteFactory(() {
    MVStoreModule storeModule = MVStoreModule.withConfig()
        .filePath('/tmp/test-old.db')
        .build();
    
    return Nitrite.builder()
        .compressed()
        .loadModule(storeModule)
        .openOrCreate();
});
exportOptions.setCollections(List.of("first"));
exportOptions.setRepositories(List.of("org.dizitart.no2.support.data.Employee"));
exportOptions.setKeyedRepositories(Map.of("key", Set.of("org.dizitart.no2.support.data.Employee")));

Exporter exporter = Exporter.withOptions(exportOptions);
exporter.exportTo(schemaFile);
```

### Import Data

```java
// import data from a json file
ImportOptions importOptions = new ImportOptions();
importOptions.setNitriteFactory(() {
    MVStoreModule storeModule = MVStoreModule.withConfig()
        .filePath('/tmp/test-old.db')
        .build();

    return Nitrite.builder()
        .compressed()
        .loadModule(storeModule)
        .openOrCreate();
});

Importer importer = Importer.withOptions(importOptions);
importer.importFrom(schemaFile);
```

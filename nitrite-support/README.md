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
Exporter exporter = Exporter.of(sourceDb);
exporter.exportTo(schemaFile);
```

### Import Data

```java
// import data from a json file
Importer importer = Importer.of(destDb);
importer.importFrom(schemaFile);
```

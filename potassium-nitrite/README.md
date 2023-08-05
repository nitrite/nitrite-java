# Potassium Nitrite

A kotlin extension for Nitrite database. It aims to streamline the usage of nitrite with kotlin by leveraging its 
language features like extension function, builders, infix functions etc.

## How To Install

To use this add required dependencies:

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>potassium-nitrite</artifactId>
    </dependency>
</dependencies>

```

**Gradle**

```groovy
implementation 'org.dizitart:potassium-nitrite'

```

## How To Use

### Initialize Nitrite Database

```kotlin
val db = nitrite("user", "password") {
    loadModule(MVStoreModule(fileName))
    loadModule(module(NitriteTextIndexer(UniversalTextTokenizer())))
}
```

More details can be found in the documentation.
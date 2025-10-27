# Nitrite Database

[![Build](https://github.com/nitrite/nitrite-java/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/nitrite/nitrite-java/actions/workflows/build.yml)
[![CodeQL](https://github.com/nitrite/nitrite-java/actions/workflows/codeql-analysis.yml/badge.svg?branch=main)](https://github.com/nitrite/nitrite-java/actions/workflows/codeql-analysis.yml)
[![codecov](https://codecov.io/gh/nitrite/nitrite-java/branch/main/graph/badge.svg)](https://codecov.io/gh/nitrite/nitrite-java)
[![javadoc](https://javadoc.io/badge2/org.dizitart/nitrite/javadoc.svg)](https://javadoc.io/doc/org.dizitart/nitrite) 
[![Discussion](https://img.shields.io/badge/chat-Discussion-blueviolet)](https://github.com/orgs/nitrite/discussions)

<p align="center">
    <img src="assets/nitrite-logo.svg" width="256" alt="nitrite logo">
</p>

**NO**sql **O**bject (**NO<sub>2</sub>** a.k.a Nitrite) database is an open source nosql embedded
document store. It supports both in-memory and file based persistent store.

Nitrite is an embedded database ideal for desktop, mobile or small web applications.

**It features**:

- Embedded, serverless
- Simple API
- Document-oriented
- Schemaless document collection and object repository
- Extensible storage engines - mvstore, rocksdb
- Indexing and full-text search
- Simple query api
- In-memory and file-based store
- Transaction support
- Schema migration support
- Encryption support
- Android compatibility (API Level 26)

## Kotlin Extension

Nitrite has a kotlin extension called **Potassium Nitrite** for kotlin developers.
Visit [here](https://github.com/nitrite/nitrite-java/tree/main/potassium-nitrite) for more details.

## Flutter Version

If you are looking for Nitrite for Flutter/Dart, head over to [nitrite-flutter](https://github.com/nitrite/nitrite-flutter).

## Deprecation Notice

Nitrite DataGate and Nitrite Explorer is now deprecated and no longer maintained.

## Getting Started with Nitrite

**NOTE:** There are breaking api changes in version 4.x. So please read the [guide](https://nitrite.dizitart.com/java-sdk/getting-started/index.html) before upgrading from 3.x.x.

### How To Install

To use Nitrite in any Java application, first add the nitrite bill of materials, then add required dependencies:

**Maven**

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.dizitart</groupId>
            <artifactId>nitrite-bom</artifactId>
            <version>[latest version]</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>nitrite</artifactId>
    </dependency>

    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>nitrite-mvstore-adapter</artifactId>
    </dependency>
</dependencies>
```
    
**Gradle**

```groovy

implementation(platform("org.dizitart:nitrite-bom:[latest version]"))
    
implementation 'org.dizitart:nitrite'
implementation 'org.dizitart:nitrite-mvstore-adapter'

```
    
## Examples

A Todo android application is available [here](https://github.com/nitrite/nitrite-android-demo) to demonstrate the usage of Nitrite in android.

### Quick Examples

**Initialize Database**

```java
// create a mvstore backed storage module
MVStoreModule storeModule = MVStoreModule.withConfig()
    .filePath("/tmp/test.db") 
    .compress(true)
    .build();

// or a rocksdb based storage module
RocksDBModule storeModule = RocksDBModule.withConfig()
    .filePath("/tmp/test.db")
    .build();


// initialization using builder
Nitrite db = Nitrite.builder()
        .loadModule(storeModule)
        .loadModule(new JacksonMapperModule())  // optional
        .openOrCreate("user", "password");

```

**Create a Collection**

```java
// Create a Nitrite Collection
NitriteCollection collection = db.getCollection("test");

// Create an Object Repository
ObjectRepository<Employee> repository = db.getRepository(Employee.class);

```

**Annotations for POJO**

```java

@Entity(value = "retired-employee",     // entity name (optional), 
    indices = {
        @Index(value = "firstName", type = IndexType.NON_UNIQUE),
        @Index(value = "lastName", type = IndexType.NON_UNIQUE),
        @Index(value = "note", type = IndexType.FULL_TEXT),
})
public class Employee implements Serializable {
    // provides id field to uniquely identify an object inside an ObjectRepository
    @Id
    private long empId;
    private Date joinDate;
    private String firstName;
    private String lastName;
    private String note;

    // ... public getters and setters
}

```


**CRUD Operations**

```java

// create a document to populate data
Document doc = Document.createDocument("firstName", "John")
     .put("lastName", "Doe")
     .put("birthDay", new Date())
     .put("data", new byte[] {1, 2, 3})
     .put("fruits", new ArrayList<String>() {{ add("apple"); add("orange"); add("banana"); }})
     .put("note", "a quick brown fox jump over the lazy dog");

// insert the document
collection.insert(doc);

// find a document
collection.find(where("firstName").eq("John").and(where("lastName").eq("Doe"));

// update the document
collection.update(where("firstName").eq("John"), createDocument("lastName", "Wick"));

// remove the document
collection.remove(doc);

// insert an object in repository
Employee emp = new Employee();
emp.setEmpId(124589);
emp.setFirstName("John");
emp.setLastName("Doe");

repository.insert(emp);

```

**Create Indices**

```java

// create document index
collection.createIndex(indexOptions(IndexType.NON_UNIQUE), "firstName", "lastName"); // compound index
collection.createIndex(indexOptions(IndexType.FULL_TEXT), "note"); // full-text index

// create object index. It can also be provided via annotation
repository.createIndex(indexOptions(IndexType.NON_UNIQUE), "firstName");

```

**Query a Collection**

```java

DocumentCursor cursor = collection.find(
    where("firstName").eq("John")               // firstName == John
    .and(
        where("data").elemMatch("$".lt(4))      // AND elements of data array is less than 4
            .and(
                where("note").text("quick")     // AND note field contains string 'quick' using full-text index
        )       
    )
);

for (Document document : cursor) {
    // process the document
}

// get document by id
Document document = collection.getById(nitriteId);

// query an object repository and create the first result
Cursor<Employee> cursor = repository.find(where("firstName").eq("John"));
Employee employee = cursor.firstOrNull();

```

**Transaction**

```java
try (Session session = db.createSession()) {
    try (Transaction transaction = session.beginTransaction()) {
        NitriteCollection txCol = transaction.getCollection("test");

        Document document = createDocument("firstName", "John");
        txCol.insert(document);

        transaction.commit();
    } catch (TransactionException e) {
        transaction.rollback();
    }
}


```

**Schema Migration**

```java

Migration migration1 = new Migration(Constants.INITIAL_SCHEMA_VERSION, 2) {
    @Override
    public void migrate(InstructionSet instructions) {
        instructions.forDatabase()
            // make a non-secure db to secure db
            .addUser("test-user", "test-password");

        // create instructions for existing repository
        instructions.forRepository(OldClass.class, "demo1")

            // rename the repository (in case of entity name changes)
            .renameRepository("migrated", null)

            // change datatype of field empId from String to Long and convert the values
            .changeDataType("empId", (TypeConverter<String, Long>) Long::parseLong)

            // change id field from uuid to empId
            .changeIdField(Fields.withNames("uuid"), Fields.withNames("empId"))

            // delete uuid field
            .deleteField("uuid")
    
            // rename field from lastName to familyName
            .renameField("lastName", "familyName")

            // add new field fullName and add default value as - firstName + " " + lastName
            .addField("fullName", document -> document.get("firstName", String.class) + " "
                + document.get("familyName", String.class))

            // drop index on firstName
            .dropIndex("firstName")

            // drop index on embedded field literature.text
            .dropIndex("literature.text")

            // change data type of embedded field from float to integer and convert the values 
            .changeDataType("literature.ratings", (TypeConverter<Float, Integer>) Math::round);
    }
};

Migration migration2 = new Migration(2, 3) {
    @Override
    public void migrate(InstructionSet instructions) {
        instructions.forCollection("test")
            .addField("fullName", "Dummy Name");
    }
};

MVStoreModule storeModule = MVStoreModule.withConfig()
    .filePath("/temp/employee.db")
    .compressHigh(true)
    .build();

db = Nitrite.builder()
    .loadModule(storeModule)
    
    // schema versioning is must for migration
    .schemaVersion(2)

    // add defined migration paths
    .addMigrations(migration1, migration2)
    .openOrCreate();

```

**Import/Export Data**

```java
// Export data to json file

// create export options
ExportOptions exportOptions = new ExportOptions();
// set the nitrite factory
exportOptions.setNitriteFactory(() -> openDb("test.db"));
// set the collections to export
exportOptions.setCollections(List.of("first"));
// set the repositories to export
exportOptions.setRepositories(List.of("org.dizitart.no2.support.data.Employee", "org.dizitart.no2.support.data.Company"));
// set the keyed repositories to export
exportOptions.setKeyedRepositories(Map.of("key", Set.of("org.dizitart.no2.support.data.Employee")));
// create an exporter with export options
Exporter exporter = Exporter.withOptions(exportOptions);
exporter.exportTo("test.json");

// Import data from the file

// create import options
ImportOptions importOptions = new ImportOptions();
// set the nitrite factory
importOptions.setNitriteFactory(() -> openDb("new-test.db"));
// create an importer with import options
Importer importer = Importer.withOptions(importOptions);
importer.importFrom("test.json");

```

More details are available in the [guide](https://nitrite.dizitart.com/java-sdk/getting-started/index.html).

## Release Notes

Release notes are available [here](https://github.com/nitrite/nitrite-java/releases).

## Documentation

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Reference</th>
<th>API</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p><a href="https://nitrite.dizitart.com/java-sdk/getting-started/index.html">Document</a></p></td>
<td><p><a href="https://javadoc.io/doc/org.dizitart/nitrite">JavaDoc</a></p></td>
</tr>
</tbody>
</table>

## Build

To build and test Nitrite, ensure you have JDK 11 (or higher) and Maven 3 installed.

```shell script

git clone https://github.com/nitrite/nitrite-java.git
cd nitrite-java
mvn clean install

```

## Support / Feedback

For issues with, questions about, or feedback create a [discussion](https://github.com/orgs/nitrite/discussions).

## Bugs / Feature Requests

Think you’ve found a bug? Want to see a new feature in the Nitrite? Please open an issue [here](https://github.com/nitrite/nitrite-java/issues). But
before you file an issue please check if it is already existing or not.

## Maintainers

-   Anindya Chatterjee

## Contributors

This project exists thanks to all the people who contribute. For more details please visit [CONTRIBUTING.md](https://github.com/nitrite/nitrite-java/blob/main/CONTRIBUTING.md).

## Sponsors

Support this project by becoming a sponsor. Your logo will show up here with a link to your website. [Become a sponsor](https://github.com/sponsors/anidotnet) for this project.

## Presentation & Talks

[Idan Sheinberg](https://github.com/sheinbergon) has given a talk on Nitrite at [**Kotlin Everywhere - TLV Edition**](https://www.meetup.com/KotlinTLV/events/265145254/) meetup on October 27, 2019. Please find his presentation [here](https://www.slideshare.net/IdanShinberg/nitrite-choosing-the-rite-embedded-database).

## Special Thanks

<div>
<a href="https://www.ej-technologies.com/products/jprofiler/overview.html" style="margin-right: 20px;">
    <img src="https://www.ej-technologies.com/images/product_banners/jprofiler_medium.png" alt="JProfiler"/>
</a>

<a href="https://www.yourkit.com/" style="margin-right: 20px;">
    <img src="https://www.yourkit.com/images/yklogo.png" alt="YourKit"/>
</a>
   
<a href="https://www.macstadium.com/" style="margin-right: 30px;">
    <img src="https://uploads-ssl.webflow.com/5ac3c046c82724970fc60918/5c019d917bba312af7553b49_MacStadium-developerlogo.png" height="32" alt="MacStadium"/>
</a>
</div>

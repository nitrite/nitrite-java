# Nitrite Database

![Build](https://github.com/nitrite/nitrite-java/workflows/Gradle%20Build/badge.svg?branch=develop)
![CodeQL](https://github.com/nitrite/nitrite-java/workflows/CodeQL/badge.svg?branch=develop)
[![Codacy](https://app.codacy.com/project/badge/Grade/3ee6a6f3f0044b0c9e75d48e47e5d012)](https://www.codacy.com/gh/nitrite/nitrite-java/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=nitrite/nitrite-java&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/nitrite/nitrite-java/branch/develop/graph/badge.svg)](https://codecov.io/gh/nitrite/nitrite-java)
![Javadocs](https://javadoc.io/badge/org.dizitart/nitrite.svg)
[![Discussion](https://img.shields.io/badge/chat-Discussion-blueviolet)](https://github.com/nitrite/nitrite-java/discussions)
![Backers on Open Collective](https://opencollective.com/nitrite-database/backers/badge.svg)
![Backers on Open Collective](https://opencollective.com/nitrite-database/sponsors/badge.svg)
[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/nitrite/nitrite-java)

<img src="http://www.dizitart.org/nitrite-database/logo/nitrite-logo.svg" alt="Logo" width="200"/>

**NO**sql **O**bject (**NO<sub>2</sub>** a.k.a Nitrite) database is an open source nosql embedded
document store written in Java. It has MongoDB like API. It supports both
in-memory and file based persistent store.

Nitrite is an embedded database ideal for desktop, mobile or small web applications.

**It features**:

-   Schemaless document collection and object repository
-   In-memory / file-based store
-   Pluggable storage engines - mvstore, mapdb, rocksdb
-   ACID transaction
-   Schema migration
-   Indexing
-   Full text search
-   Both way replication via Nitrite DataGate server
-   Very fast, lightweight and fluent API 
-   Android compatibility (API Level 19)

## Kotlin Extension

Nitrite has a kotlin extension called **Potassium Nitrite** for kotlin developers.
Visit [here](https://github.com/nitrite/nitrite-java/tree/develop/potassium-nitrite) for more details.

## Getting Started with Nitrite

**NOTE:** There are breaking api changes in version 4.x.x. So please exercise caution when upgrading from 3.x.x  
especially for **package name changes**.

### How To Install

To use Nitrite in any Java application, first add the nitrite bill of materials, 
then add required dependencies:

**Maven**

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.dizitart</groupId>
            <artifactId>nitrite-bom</artifactId>
            <version>4.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
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

implementation(platform("org.dizitart:nitrite-bom:4.0.0-SNAPSHOT"))
    
implementation 'org.dizitart:nitrite'
implementation 'org.dizitart:nitrite-mvstore-adapter'

```
    

### Quick Examples

**Initialize Database**

```java
// create a mvstore backed storage module
MVStoreModule storeModule = MVStoreModule.withConfig()
    .filePath("/tmp/test.db")  // for android - .filePath(getFilesDir().getPath() + "/test.db")
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
        @Index(value = "firstName", type = IndexType.NonUnique),
        @Index(value = "lastName", type = IndexType.NonUnique),
        @Index(value = "note", type = IndexType.Fulltext),
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
Document doc = createDocument("firstName", "John")
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
collection.createIndex("firstName", indexOptions(IndexType.NonUnique));
collection.createIndex("note", indexOptions(IndexType.Fulltext));

// create object index. It can also be provided via annotation
repository.createIndex("firstName", indexOptions(IndexType.NonUnique));

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
    Transaction transaction = session.beginTransaction();
    try {
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
    public void migrate(Instruction instructions) {
        instructions.forDatabase()
            // make a non-secure db to secure db
            .addPassword("test-user", "test-password");

        // create instructions for existing repository
        instructions.forRepository(OldClass.class, null)

            // rename the repository (in case of entity name changes)
            .renameRepository("migrated", null)

            // change datatype of field empId from String to Long and convert the values
            .changeDataType("empId", (TypeConverter<String, Long>) Long::parseLong)

            // change id field from uuid to empId
            .changeIdField("uuid", "empId")

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
    public void migrate(Instruction instructions) {
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

**Automatic Replication**

```java

NitriteCollection collection = db.getCollection("products");

Replica replica = Replica.builder()
    .of(collection)
    // replication via websocket (ws/wss)
    .remote("ws://127.0.0.1:9090/datagate/john/products")
    // user authentication via JWT token
    .jwtAuth("john", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    .create();

replica.connect();

```

**Import/Export Data**

```java
// Export data to a file
Exporter exporter = Exporter.of(db);
exporter.exportTo(schemaFile);

//Import data from the file
Importer importer = Importer.of(db);
importer.importFrom(schemaFile);

```

More details are available in the reference document.

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
<td><p><a href="http://www.dizitart.org/nitrite-database">Document</a></p></td>
<td><p><a href="https://javadoc.io/doc/org.dizitart/nitrite">JavaDoc</a></p></td>
</tr>
</tbody>
</table>

## Build

To build and test Nitrite

```shell script

git clone https://github.com/nitrite/nitrite-java.git
cd nitrite-java
./gradlew build

```

## Support / Feedback

For issues with, questions about, or feedback talk to us at [Gitter](https://gitter.im/nitrite-db/nitrite-java).

## Bugs / Feature Requests

Think you’ve found a bug? Want to see a new feature in the Nitrite? Please open an issue [here](https://github.com/nitrite/nitrite-java/issues). But
before you file an issue please check if it is already existing or not.

## Maintainers

-   Anindya Chatterjee

## Contributors

This project exists thanks to all the people who contribute. [Contribute](https://github.com/dizitart/nitrite-database/blob/master/CONTRIBUTING.md).
![Contributors](https://opencollective.com/nitrite-database/contributors.svg?width=890)

## Backers

Thank you to all our backers! 🙏 [Become a backer](https://opencollective.com/nitrite-database#backer)

![Backers](https://opencollective.com/nitrite-database/backers.svg?width=890)

## Sponsors

Support this project by becoming a sponsor. Your logo will show up here with a link to your website. [Become a sponsor](https://opencollective.com/nitrite-database#sponsor)

![Sponsor](https://opencollective.com/nitrite-database/sponsor/0/avatar.svg)
![Sponsor](https://opencollective.com/nitrite-database/sponsor/1/avatar.svg)
![Sponsor](https://opencollective.com/nitrite-database/sponsor/2/avatar.svg)
![Sponsor](https://opencollective.com/nitrite-database/sponsor/3/avatar.svg)
![Sponsor](https://opencollective.com/nitrite-database/sponsor/4/avatar.svg)

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

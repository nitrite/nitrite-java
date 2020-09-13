# Nitrite Database

![Build Status](https://travis-ci.org/dizitart/nitrite-database.svg?branch=master)
![Coverage Status](https://codecov.io/gh/dizitart/nitrite-database/branch/master/graph/badge.svg)
![Javadocs](https://javadoc.io/badge/org.dizitart/nitrite.svg)
![Gitter](https://badges.gitter.im/dizitart/nitrite-database.svg)
![Backers on Open Collective](https://opencollective.com/nitrite-database/backers/badge.svg)
![Backers on Open Collective](https://opencollective.com/nitrite-database/sponsors/badge.svg)

![Logo 200](http://www.dizitart.org/nitrite-database/logo/nitrite-logo.svg)

**NO**sql **O**bject (**NO<sub>2</sub>** a.k.a Nitrite) database is an open source nosql embedded
document store written in Java. It has MongoDB like API. It supports both
in-memory and single file based persistent store powered by
[MVStore](http://www.h2database.com/html/mvstore.html) engine of h2 database.

Nitrite is a server-less embedded database ideal for desktop, mobile or small web applications.

**It features**:

-   Embedded key-value/document and object store

-   In-memory off-heap store

-   Single file store

-   Very fast and lightweight MongoDB like API

-   Indexing

-   Full text search capability

-   Full Android compatibility (API Level 19)

-   Observable store

-   Both way replication via Nitrite DataGate server

## Kotlin Extension

Nitrite has a kotlin extension called **Potassium Nitrite** for kotlin developers.
Visit [here](https://github.com/dizitart/nitrite-database/tree/master/potassium-nitrite) for more details.

## Data Explorer

To view the data of a nitrite database file, use **Nitrite Explorer**. More details
can be found [here](https://github.com/dizitart/nitrite-database/tree/master/nitrite-explorer).

## Data Replication

To replicate data over different devices automatically, use **Nitrite DataGate** server. For more details
visit [here](https://github.com/dizitart/nitrite-database/tree/master/nitrite-datagate).

## Getting Started with Nitrite

### How To Install

To use Nitrite in any Java application, just add the below dependency:

**Maven**

    <dependency>
        <groupId>org.dizitart</groupId>
        <artifactId>nitrite</artifactId>
        <version>{version}</version>
    </dependency>

**Gradle**

    implementation 'org.dizitart:nitrite:{version}'

### Quick Examples

**Initialize Database**

    //java initialization
    Nitrite db = Nitrite.builder()
            .compressed()
            .filePath("/tmp/test.db")
            .openOrCreate("user", "password");

    //android initialization
    Nitrite db = Nitrite.builder()
            .compressed()
            .filePath(getFilesDir().getPath() + "/test.db")
            .openOrCreate("user", "password");

**Create a Collection**

    // Create a Nitrite Collection
    NitriteCollection collection = db.getCollection("test");

    // Create an Object Repository
    ObjectRepository<Employee> repository = db.getRepository(Employee.class);

**Annotations for POJO**

    // provides index information for ObjectRepository
    @Indices({
            @Index(value = "joinDate", type = IndexType.NonUnique),
            @Index(value = "name", type = IndexType.Unique)
    })
    public class Employee implements Serializable {
        // provides id field to uniquely identify an object inside an ObjectRepository
        @Id
        private long empId;

        private Date joinDate;

        private String name;

        private String address;

        // ... public getters and setters
    }

**CRUD Operations**

    // create a document to populate data
    Document doc = createDocument("firstName", "John")
         .put("lastName", "Doe")
         .put("birthDay", new Date())
         .put("data", new byte[] {1, 2, 3})
         .put("fruits", new ArrayList<String>() {{ add("apple"); add("orange"); add("banana"); }})
         .put("note", "a quick brown fox jump over the lazy dog");

    // insert the document
    collection.insert(doc);

    // update the document
    collection.update(eq("firstName", "John"), createDocument("lastName", "Wick"));

    // remove the document
    collection.remove(doc);

    // insert an object
    Employee emp = new Employee();
    emp.setEmpId(124589);
    emp.setFirstName("John");
    emp.setLastName("Doe");

    repository.insert(emp);

**Create Indices**

    // create document index
    collection.createIndex("firstName", indexOptions(IndexType.NonUnique));
    collection.createIndex("note", indexOptions(IndexType.Fulltext));

    // create object index. It can also be provided via annotation
    repository.createIndex("firstName", indexOptions(IndexType.NonUnique));

**Query a Collection**

    Cursor cursor = collection.find(
                            // and clause
                            and(
                                // firstName == John
                                eq("firstName", "John"),
                                // elements of data array is less than 4
                                elemMatch("data", lt("$", 4)),
                                // elements of fruits list has one element matching orange
                                elemMatch("fruits", regex("$", "orange")),
                                // note field contains string 'quick' using full-text index
                                text("note", "quick")
                                )
                            );

    for (Document document : cursor) {
        // process the document
    }

    // create document by id
    Document document = collection.getById(nitriteId);

    // query an object repository and create the first result
    Employee emp = repository.find(eq("firstName", "John"))
                             .firstOrDefault();

**Automatic Replication**

    // connect to a DataGate server running at localhost 9090 port
    DataGateClient dataGateClient = new DataGateClient("http://localhost:9090")
            .withAuth("userId", "password");
    DataGateSyncTemplate syncTemplate
            = new DataGateSyncTemplate(dataGateClient, "remote-collection@userId");

    // create sync handle
    SyncHandle syncHandle = Replicator.of(db)
            .forLocal(collection)
            // a DataGate sync template implementation
            .withSyncTemplate(syncTemplate)
            // replication attempt delay of 1 sec
            .delay(timeSpan(1, TimeUnit.SECONDS))
            // both-way replication
            .ofType(ReplicationType.BOTH_WAY)
            // sync event listener
            .withListener(new SyncEventListener() {
                @Override
                public void onSyncEvent(SyncEventData eventInfo) {

                }
            })
            .configure();

    // start sync in the background using handle
    syncHandle.startSync();

**Import/Export Data**

    // Export data to a file
    Exporter exporter = Exporter.of(db);
    exporter.exportTo(schemaFile);

    //Import data from the file
    Importer importer = Importer.of(db);
    importer.importFrom(schemaFile);

More details are available in the reference document.

## Release Notes

Release notes are available [here](https://github.com/dizitart/nitrite-database/releases).

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

    $ git clone https://github.com/dizitart/nitrite-database.git
    $ cd nitrite-database
    $ ./gradlew build

The test suite requires mongod to be running on localhost, listening on the default port. MongoDb is required
to test replication using the DataGate server. Please run the below command to create the test user in mongo.

    db.getSiblingDB('benchmark').createUser({user: 'bench', pwd: 'bench', roles: [{role: 'readWrite', db: 'benchmark'}, {role: 'dbAdmin', db: 'benchmark'}]})

The test suite also requires android sdk 26 to be installed and ANDROID\_HOME environment variable to be setup
properly to test the android example.

## Support / Feedback

For issues with, questions about, or feedback talk to us at [Gitter](https://gitter.im/dizitart/nitrite-database).

## Bugs / Feature Requests

Think you’ve found a bug? Want to see a new feature in the Nitrite? Please open an issue [here](https://github.com/dizitart/nitrite-database/issues). But
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

![YourKit](https://www.yourkit.com/images/yklogo.png)

I highly recommend YourKit Java Profiler for any performance critical application you make.

Check it out at <https://www.yourkit.com/>

= Nitrite Database

image:https://liberapay.com/assets/widgets/donate.svg["Donate using Liberapay", link="https://liberapay.com/anidotnet/donate"]
image:https://travis-ci.org/dizitart/nitrite-database.svg?branch=master["Build Status", link="https://travis-ci.org/dizitart/nitrite-database"]
image:https://codecov.io/gh/dizitart/nitrite-database/branch/master/graph/badge.svg["Coverage Status", link="https://codecov.io/gh/dizitart/nitrite-database"]
image:https://javadoc.io/badge/org.dizitart/nitrite.svg["Javadocs", link=https://javadoc.io/doc/org.dizitart/nitrite]
image:https://badges.gitter.im/dizitart/nitrite-database.svg["Gitter", link="https://gitter.im/dizitart/nitrite-database?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=body_badge"]
image:https://opencollective.com/nitrite-database/backers/badge.svg["Backers on Open Collective", link="#backers"]
image:https://opencollective.com/nitrite-database/sponsors/badge.svg["Backers on Open Collective", link="#sponsors"]

image:http://www.dizitart.org/nitrite-database/logo/nitrite-logo.svg[Logo 200, 200]

**NO**sql **O**bject (*NO~2~* a.k.a Nitrite) database is an open source nosql embedded
document store written in Java. It has MongoDB like API. It supports both
in-memory and single file based persistent store powered by
http://www.h2database.com/html/mvstore.html[MVStore] engine of h2 database.

Nitrite is a server-less embedded database ideal for desktop, mobile or small web applications.

**It features**:

* Embedded key-value/document and object store
* In-memory or single data file
* Very fast and lightweight MongoDB like API
* Indexing
* Full text search capability
* Full Android compatibility
* Observable store
* Both way replication via Nitrite DataGate server

== Kotlin Extension

Nitrite has a kotlin extension called **Potassium Nitrite** for kotlin developers.
Visit https://github.com/dizitart/nitrite-database/tree/master/potassium-nitrite[here] for more details.

== Data Explorer

To view the data of a nitrite database file, use **Nitrite Explorer**. More details
can be found https://github.com/dizitart/nitrite-database/tree/master/nitrite-explorer[here].

== Data Replication

To replicate data over different devices automatically, use **Nitrite DataGate** server. For more details
visit https://github.com/dizitart/nitrite-database/tree/master/nitrite-datagate[here].

== Getting Started with Nitrite

=== How To Install

To use Nitrite in any Java application, just add the below dependency:

*Maven*

[source,xml,subs="verbatim,attributes"]
----
<dependency>
    <groupId>org.dizitart</groupId>
    <artifactId>nitrite</artifactId>
    <version>{version}</version>
</dependency>
----

*Gradle*

[source,groovy,subs="verbatim,attributes"]
----
compile 'org.dizitart:nitrite:{version}'
----

<<<

=== Quick Examples

*Initialize Database*
[source,java]
--
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
--

*Create a Collection*
[source,java]
--
// Create a Nitrite Collection
NitriteCollection collection = db.getCollection("test");

// Create an Object Repository
ObjectRepository<Employee> repository = db.getRepository(Employee.class);

--

*Annotations for POJO*
[source,java]
--
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

--


*CRUD Operations*
[source,java]
--
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
--

[source,java]
--
// insert an object
Employee emp = new Employee();
emp.setEmpId(124589);
emp.setFirstName("John");
emp.setLastName("Doe");

repository.insert(emp);

--

*Create Indices*
[source,java]
--
// create document index
collection.createIndex("firstName", indexOptions(IndexType.NonUnique));
collection.createIndex("note", indexOptions(IndexType.Fulltext));

// create object index. It can also be provided via annotation
repository.createIndex("firstName", indexOptions(IndexType.NonUnique));
--

*Query a Collection*
[source,java]
--
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
--

*Automatic Replication*
[source,java]
--
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
--

*Import/Export Data*
[source,java]
--
// Export data to a file
Exporter exporter = Exporter.of(db);
exporter.exportTo(schemaFile);

//Import data from the file
Importer importer = Importer.of(db);
importer.importFrom(schemaFile);
--

More details are available in the reference document.

== Release Notes

Release notes are available https://github.com/dizitart/nitrite-database/releases[here].

== Documentation

|===
|Reference |API

|http://www.dizitart.org/nitrite-database[Document]
|https://javadoc.io/doc/org.dizitart/nitrite[JavaDoc]
|===


== Build

To build and test Nitrite

[source,bash]
--
$ git clone https://github.com/dizitart/nitrite-database.git
$ cd nitrite-database
$ ./gradlew build
--

The test suite requires mongod to be running on localhost, listening on the default port. MongoDb is required
to test replication using the DataGate server. Please run the below command to create the test user in mongo.

[source,javascript]
--
db.getSiblingDB('benchmark').createUser({user: 'bench', pwd: 'bench', roles: [{role: 'readWrite', db: 'benchmark'}, {role: 'dbAdmin', db: 'benchmark'}]})
--

The test suite also requires android sdk 24.4.1 to be installed and ANDROID_HOME environment variable to be setup
properly to test the android example.

== Support / Feedback

For issues with, questions about, or feedback talk to us at https://gitter.im/dizitart/nitrite-database[Gitter].

== Bugs / Feature Requests

Think you’ve found a bug? Want to see a new feature in the Nitrite? Please open an issue https://github.com/dizitart/nitrite-database/issues[here]. But
before you file an issue please check if it is already existing or not.

== Maintainers

* Anindya Chatterjee

== Contributors

This project exists thanks to all the people who contribute. https://github.com/dizitart/nitrite-database/blob/master/CONTRIBUTING.md[Contribute].
image:https://opencollective.com/nitrite-database/contributors.svg?width=890["Contributors", link="https://github.com/dizitart/nitrite-database/graphs/contributors"]

== Backers

Thank you to all our backers! 🙏 https://opencollective.com/nitrite-database#backer[Become a backer]

image:https://opencollective.com/nitrite-database/backers.svg?width=890["Backers", link="https://opencollective.com/nitrite-database#backers"]

== Sponsors

Support this project by becoming a sponsor. Your logo will show up here with a link to your website. https://opencollective.com/nitrite-database#sponsor[Become a sponsor]

image:https://opencollective.com/nitrite-database/sponsor/0/avatar.svg["Sponsor", link="https://opencollective.com/nitrite-database/sponsor/0/website"]
image:https://opencollective.com/nitrite-database/sponsor/1/avatar.svg["Sponsor", link="https://opencollective.com/nitrite-database/sponsor/1/website"]
image:https://opencollective.com/nitrite-database/sponsor/2/avatar.svg["Sponsor", link="https://opencollective.com/nitrite-database/sponsor/2/website"]
image:https://opencollective.com/nitrite-database/sponsor/3/avatar.svg["Sponsor", link="https://opencollective.com/nitrite-database/sponsor/3/website"]
image:https://opencollective.com/nitrite-database/sponsor/4/avatar.svg["Sponsor", link="https://opencollective.com/nitrite-database/sponsor/4/website"]
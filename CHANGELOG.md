## Release 4.3.0

### New Changes

- Nitrite now supports JPMS. It is now modular and can be used in Java 9 or above.
- Version upgrade for several dependencies
- Repository type validation can be disabled in `NitriteBuilder` as a fix for #966

### Issue Fixes

- Fix for #935
- Fix for #948
- Fix for #961
- Fix for #966

## Release 4.2.2 - Mar 5, 2024

### Issue Fixes

- Fix for #917
- Fix for #916
- Fix for #911
- Version upgrade for several dependencies

## Release 4.2.1 - Feb 19, 2024

### Issue Fixes

- Fix for #901
- Fix for #902
- Version upgrade for several dependencies

## Release 4.2.0 - Jan 6, 2024

### New Changes

- Nitrite API has been re-written from ground up. It is now more stable and performant. But there are breaking changes. Please read the [guide](https://nitrite.dizitart.com/) for more details.
- Nitrite now requires Java 11 or above.
- Nitrite is now modular. It has been now divided into several modules. You can use only the modules you need.
- Modular storage adapters are now available. You can use only the storage adapter you need.
- MVStore version upgraded to 2.2.224
- RocksDB has been introduced as a new storage adapter.
- Nitrite now supports transaction.
- Nitrite now supports schema migration.
- Nitrite now supports spatial indexing and search
- Nitrite now supports compound indexes.
- Nitrite now support import/export of data in JSON format.
- Build system has been migrated to Maven.
- Nitrite DataGate has been deprecated.
- Nitrite Explorer has been deprecated.

## Release 3.4.4 - Mar 23, 2022

### Issue Fixes

- Emergency fix for #697

## Release 3.4.3 - Dec 12, 2020

### Issue Fixes

- Random crashes with exception "Fatal Exception: java.lang.IllegalStateException: Chunk 55267 not found" #386
- Null pointer on updating full text index #366
- Breton list is actually Brazilian Portuguese #251

## Release 3.4.2 - Jun 2, 2020

### Issue Fixes

- Fix for NoClassDefFoundError in isObjectStore #220
- Fix for Full text index is not updated field update #222

## Release 3.4.1 - Mar 25, 2020

### Issue Fixes

- Fix for Deadlock in latest 3.4.0 #212

## Release 3.4.0 - Mar 24, 2020

### Issue Fixes

- Fix for UniqueConstraintException when upserting #193
- Fix for several NPEs under certain edge case conditions #203
- Fix for Off-Heap store memory utilization issues #211

## Release 3.3.0 - Oct 19, 2019

### New Changes

- Upgrade MVStore version to 1.4.200
- Add Support for Off-Heap Memory #160
- Offer close und update methods for TextIndexingService #176
- Allow to access collection of IDs from find result #165
- Sorting with accents #144

### Issue Fixes

- Null pointer exception when querying data #185
- Documentation : support for querying embedded objects #157
- Documentation: minSdkVersion should be 19 #167
- Index not removed for fulltext-indexed field when using a third-party TextIndexingService #174
- Performance enhancements for InFilter() #173
- Filtering on indexed fields with multiple Number only retrieves same type as given Comparable #178
- Unique constraints apparently not checked when updating document #151

## Release 3.2.0 - Mar 16, 2019

### New Changes

- Upgrade MVStore version to 1.4.198 #134
- Improve `Mappable` performance using constructor cache #133
- Make `ObjectRepository` and `NitriteCollection` implements `Closeable` #108

### Issue Fixes

- Database file remains locked after failed connection #116
- Exception when removing a document on a text indexed collection #114
- NitriteBuilder openOrCreate returns silently null #112

## Release 3.1.0 - Sep 1, 2018

### New Changes

- Keyed `ObjectRepository` support #78
- Podam version upgraded to resolve missing JAX-WS dependency in Java 9 #90
- MVStore upgraded to latest release #69
- Introduced a utility method to register jackson modules in `NitriteBuilder` #94
- Null order support during sort #98
- `@InheritIndices` now works for fields with any modifier #101

### Issue Fixes

- Fixed documentation for MapperFacade #100
- Added documentation for @NitriteId annotation #102
- Changes to text index not saved correctly #105
- Closing the database recreates dropped collections #106

## Release 3.0.2 - Aug 2, 2018

### Issue Fixes

- Recover should return success/failure #89
- Reopening issue #72, with variation of failing scenario still broken in 3.0.1 #93

## Release 3.0.1 - Jul 21, 2018

### New Changes

- Jackson modules are auto discoverable #68
- Refactoring of NitriteMapper #74
- Make runtime shutdown hook optional #84

### Issue Fixes

- Fix for order by using a nullable columns #72
- Fix for DataGate server for Windows #71
- Intermittent NPE in remove #76
- Fix for NPE in indexing #77
- Documentation for POJO annotation #81

## Release 3.0.0 - Apr 8, 2018

### New Changes

- `KNO2JacksonMapper` is now extendable
- Support for `NitriteId` as id field of an object
- Object's property can be updated with null
- Support for `java.time` & it's backport
- Change in update operation behavior (breaking changes)

### Issue Fixes

- ConcurrentModificationException in `NitriteEventBus` - #52
- Duplicate `@Id` in concurrent modification - #55
- Fixed a race condition while updating the index entries - #58
- Fix for sort operation - #62
- Version upgraded for several dependencies - #64

## Release 2.1.1 - Feb 4, 2018

### New Changes

- Kotlin version upgrade to 1.2.20
- Data import export extension added in potassium-nitrite

### Issue Fixes

- Fixes concurrency problem while compacting database - #41
- Lucene example fixed for update and lucene version upgraded - #44
- Fixed collection registry and repository registry - #42
- Readme updated with potassium-nitrite - #49

## Release 2.1.0 - Dec 7, 2017

### New Changes

- Introduced potassium-nitrite - kotlin extension library for nitrite
- Multi-language text tokenizer support - #36
- Cursor join - #33
- Inherit `@Id`, `@Index` annotations from super class - #37
- Default executor behaves like `CachedThreadPool` executor - #32

### Issue Fixes

- Put a check on object if it is serializable - #31

## Release 2.0.1 - Oct 24, 2017

### Issue Fixes

- Fix for SOE - #29
- Fix for sync issue - #25
- Detailed log added in `JacksonMapper`

## Release 2.0.0 - Aug 13, 2017

### New Changes

- Introduced `Mappable` interface to speed up pojo to document conversion in Android - #18 

### Breaking Changes

- `NitriteMapper` and `JacksonMapper` moved from package `org.dizitart.no2.internals` to `org.dizitart.no2.mapper`

### Issue Fixes

- Fix for `ObjectFilters.ALL` - #14
- Fix for `dropIndex()` - #22 
- Documentation added - #12, #20 

## Release 1.0.1 - Jun 1, 2017

- Minor bug fixes for DataGate server - #6 , #7 , #8 
- File parameter added while opening a database - #5 
- Documentation updated - #3 , #8 

## First Release - Apr 25, 2017

- Initial release


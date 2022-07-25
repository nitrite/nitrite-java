/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.kno2

import com.github.javafaker.Faker
import org.dizitart.no2.collection.Document
import org.dizitart.no2.collection.UpdateOptions
import org.dizitart.no2.common.meta.Attributes
import org.dizitart.no2.exceptions.NitriteIOException
import org.dizitart.no2.exceptions.TransactionException
import org.dizitart.no2.filters.FluentFilter
import org.dizitart.no2.index.IndexOptions
import org.dizitart.no2.index.IndexType
import org.dizitart.no2.mvstore.MVStoreModule
import org.dizitart.no2.transaction.Transaction
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.Consumer

/**
 *
 * @author Anindya Chatterjee
 */
class TransactionTest: BaseTest() {
    @Before
    fun before() {
        db = nitrite {
            loadModule(MVStoreModule(fileName))
        }
    }

    @Test
    fun testCommitInsert() {
        val collection = db!!.getCollection("test")
        db!!.session {
            tx {
                val txCol = getCollection("test")
                val document =
                    Document.createDocument("firstName", "John")
                txCol.insert(document)
                Assert.assertEquals(
                    txCol.find(FluentFilter.where("firstName").eq("John")).size(), 1
                )
                Assert.assertNotEquals(
                    collection.find(
                        FluentFilter.where("firstName").eq("John")
                    ).size(), 1
                )

                commit()
                Assert.assertEquals(
                    collection.find(
                        FluentFilter.where("firstName").eq("John")
                    ).size(), 1
                )
            }
        }
    }

    @Test
    fun testRollbackInsert() {
        val collection = db!!.getCollection("test")
        collection.createIndex("firstName")

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    val document =
                        Document.createDocument("firstName", "John")
                    val document2 =
                        Document.createDocument("firstName", "Jane").put("lastName", "Doe")
                    txCol.insert(document)
                    txCol.insert(document2)

                    // just to create UniqueConstraintViolation for rollback
                    collection.insert(Document.createDocument("firstName", "Jane"))
                    Assert.assertEquals(
                        txCol.find(
                            FluentFilter.where("firstName").eq("John")
                        ).size(), 1
                    )
                    Assert.assertNotEquals(
                        collection.find(
                            FluentFilter.where("lastName").eq("Doe")
                        ).size(), 1
                    )

                    commit()
                    Assert.fail()
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertNotEquals(
                        collection.find(
                            FluentFilter.where("firstName").eq("John")
                        ).size(), 1
                    )
                    Assert.assertNotEquals(
                        collection.find(
                            FluentFilter.where("lastName").eq("Doe")
                        ).size(), 1
                    )
                }
            }
        }
    }

    @Test
    fun testCommitUpdate() {
        val document = Document.createDocument("firstName", "John")
        val collection = db!!.getCollection("test")
        collection.insert(document)

        db!!.session {
            tx {
                val txCol = getCollection("test")
                document.put("lastName", "Doe")
                txCol.update(
                    FluentFilter.where("firstName").eq("John"),
                    document,
                    UpdateOptions.updateOptions(true)
                )
                Assert.assertEquals(
                    txCol.find(FluentFilter.where("lastName").eq("Doe")).size(), 1
                )
                Assert.assertNotEquals(
                    collection.find(
                        FluentFilter.where("lastName").eq("Doe")
                    ).size(), 1
                )

                commit()
                Assert.assertEquals(
                    collection.find(FluentFilter.where("lastName").eq("Doe")).size(), 1
                )
            }
        }
    }

    @Test
    fun testRollbackUpdate() {
        val collection = db!!.getCollection("test")
        collection.createIndex("firstName")
        collection.insert(Document.createDocument("firstName", "Jane"))

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    val document =
                        Document.createDocument("firstName", "John")
                    val document2 =
                        Document.createDocument("firstName", "Jane").put("lastName", "Doe")
                    txCol.update(FluentFilter.where("firstName").eq("Jane"), document2)
                    txCol.insert(document)

                    // just to create UniqueConstraintViolation for rollback
                    collection.insert(Document.createDocument("firstName", "John"))
                    Assert.assertEquals(
                        txCol.find(
                            FluentFilter.where("firstName").eq("John")
                        ).size(), 1
                    )
                    Assert.assertEquals(
                        txCol.find(FluentFilter.where("lastName").eq("Doe")).size(), 1
                    )
                    Assert.assertNotEquals(
                        collection.find(
                            FluentFilter.where("lastName").eq("Doe")
                        ).size(), 1
                    )
                    commit()
                    Assert.fail()
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertEquals(
                        collection.find(
                            FluentFilter.where("firstName").eq("Jane")
                        ).size(), 1
                    )
                    Assert.assertNotEquals(
                        collection.find(
                            FluentFilter.where("lastName").eq("Doe")
                        ).size(), 1
                    )
                }
            }
        }
    }

    @Test
    fun testCommitRemove() {
        val document = Document.createDocument("firstName", "John")
        val collection = db!!.getCollection("test")
        collection.insert(document)

        db!!.session {
            tx {
                val txCol = getCollection("test")
                txCol.remove(FluentFilter.where("firstName").eq("John"))
                Assert.assertEquals(
                    txCol.find(FluentFilter.where("firstName").eq("John")).size(), 0
                )
                Assert.assertEquals(
                    collection.find(
                        FluentFilter.where("firstName").eq("John")
                    ).size(), 1
                )
                commit()
                Assert.assertEquals(
                    collection.find(
                        FluentFilter.where("firstName").eq("John")
                    ).size(), 0
                )
            }
        }
    }

    @Test
    fun testRollbackRemove() {
        val collection = db!!.getCollection("test")
        collection.createIndex("firstName")
        val document = Document.createDocument("firstName", "John")
        collection.insert(document)

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    txCol.remove(FluentFilter.where("firstName").eq("John"))
                    Assert.assertEquals(
                        txCol.find(
                            FluentFilter.where("firstName").eq("John")
                        ).size(), 0
                    )
                    Assert.assertEquals(
                        collection.find(
                            FluentFilter.where("firstName").eq("John")
                        ).size(), 1
                    )
                    txCol.insert(Document.createDocument("firstName", "Jane"))
                    collection.insert(Document.createDocument("firstName", "Jane"))
                    commit()
                    Assert.fail()
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertEquals(
                        collection.find(
                            FluentFilter.where("firstName").eq("John")
                        ).size(), 1
                    )
                    Assert.assertEquals(
                        collection.find(
                            FluentFilter.where("firstName").eq("Jane")
                        ).size(), 1
                    )
                }
            }
        }
    }

    @Test
    fun testCommitCreateIndex() {
        val collection = db!!.getCollection("test")
        val document = Document.createDocument("firstName", "John")
        collection.insert(document)

        db!!.session {
            tx {
                val txCol = getCollection("test")
                txCol.createIndex(
                    IndexOptions.indexOptions(IndexType.FULL_TEXT),
                    "firstName"
                )
                Assert.assertTrue(txCol.hasIndex("firstName"))
                Assert.assertFalse(collection.hasIndex("firstName"))
                commit()
                Assert.assertTrue(collection.hasIndex("firstName"))
            }
        }
    }

    @Test
    fun testRollbackCreateIndex() {
        val collection = db!!.getCollection("test")
        val document = Document.createDocument("firstName", "John")
        val document2 = Document.createDocument("firstName", "Jane")
        collection.insert(document)

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    txCol.createIndex("firstName")
                    Assert.assertTrue(txCol.hasIndex("firstName"))
                    Assert.assertFalse(collection.hasIndex("firstName"))
                    txCol.insert(document2)
                    collection.insert(document2)
                    commit()
                    Assert.fail()
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertFalse(collection.hasIndex("firstName"))
                }
            }
        }
    }

    @Test
    fun testCommitClear() {
        val collection = db!!.getCollection("test")
        val document = Document.createDocument("firstName", "John")
        collection.insert(document)

        db!!.session {
            tx {
                val txCol = getCollection("test")
                txCol.clear()
                Assert.assertEquals(0, txCol.size())
                Assert.assertEquals(1, collection.size())
                commit()
                Assert.assertEquals(0, collection.size())
            }
        }
    }

    @Test
    fun testRollbackClear() {
        val collection = db!!.getCollection("test")
        collection.createIndex("firstName")
        val document = Document.createDocument("firstName", "John")
        val document2 = Document.createDocument("firstName", "Jane")
        collection.insert(document)

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    txCol.clear()
                    Assert.assertEquals(0, txCol.size())
                    Assert.assertEquals(1, collection.size())
                    txCol.insert(document2)
                    collection.insert(document2)
                    throw TransactionException("failed")
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertEquals(2, collection.size())
                }
            }
        }
    }

    @Test
    fun testCommitDropIndex() {
        val collection = db!!.getCollection("test")
        val document = Document.createDocument("firstName", "John")
        collection.insert(document)
        collection.createIndex("firstName")

        db!!.session {
            tx {
                val txCol = getCollection("test")
                txCol.dropIndex("firstName")
                Assert.assertFalse(txCol.hasIndex("firstName"))
                Assert.assertTrue(collection.hasIndex("firstName"))
                commit()
                Assert.assertFalse(collection.hasIndex("firstName"))
            }
        }
    }

    @Test
    fun testRollbackDropIndex() {
        val collection = db!!.getCollection("test")
        val document = Document.createDocument("firstName", "John").put("lastName", "Doe")
        val document2 = Document.createDocument("firstName", "Jane").put("lastName", "Doe")
        collection.insert(document)
        collection.createIndex("firstName")
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "lastName")

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    txCol.dropIndex("lastName")
                    Assert.assertFalse(txCol.hasIndex("lastName"))
                    Assert.assertTrue(collection.hasIndex("lastName"))
                    txCol.insert(document2)
                    collection.insert(document2)
                    commit()
                    Assert.fail()
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertTrue(collection.hasIndex("lastName"))
                }
            }
        }
    }

    @Test
    fun testCommitDropAllIndices() {
        val collection = db!!.getCollection("test")
        val document = Document.createDocument("firstName", "John")
        collection.insert(document)
        collection.createIndex("firstName")
        collection.createIndex("lastName")

        db!!.session {
            tx {
                val txCol = getCollection("test")
                txCol.dropAllIndices()
                Assert.assertFalse(txCol.hasIndex("firstName"))
                Assert.assertFalse(txCol.hasIndex("lastName"))
                Assert.assertTrue(collection.hasIndex("firstName"))
                Assert.assertTrue(collection.hasIndex("lastName"))
                commit()
                Assert.assertFalse(collection.hasIndex("firstName"))
                Assert.assertFalse(collection.hasIndex("lastName"))
            }
        }
    }

    @Test
    fun testRollbackDropAllIndices() {
        val collection = db!!.getCollection("test")
        val document = Document.createDocument("firstName", "John").put("lastName", "Doe")
        collection.insert(document)
        collection.createIndex("firstName")
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "lastName")

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    txCol.dropAllIndices()
                    Assert.assertFalse(txCol.hasIndex("firstName"))
                    Assert.assertFalse(txCol.hasIndex("lastName"))
                    Assert.assertTrue(collection.hasIndex("firstName"))
                    Assert.assertTrue(collection.hasIndex("lastName"))
                    txCol.insert(
                        Document.createDocument("firstName", "Jane").put("lastName", "Doe")
                    )
                    collection.insert(
                        Document.createDocument("firstName", "Jane").put("lastName", "Doe")
                    )
                    throw TransactionException("failed")
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertTrue(collection.hasIndex("firstName"))
                    Assert.assertTrue(collection.hasIndex("lastName"))
                }
            }
        }
    }

    @Test
    fun testCommitDropCollection() {
        val collection = db!!.getCollection("test")
        val document = Document.createDocument("firstName", "John")
        collection.insert(document)

        db!!.session {
            tx {
                val txCol = getCollection("test")
                txCol.drop()
                var expectedException = false
                try {
                    Assert.assertEquals(0, txCol.size())
                } catch (e: TransactionException) {
                    expectedException = true
                }
                Assert.assertTrue(expectedException)
                Assert.assertEquals(1, collection.size())
                commit()
                expectedException = false
                try {
                    Assert.assertEquals(0, collection.size())
                } catch (e: NitriteIOException) {
                    expectedException = true
                }
                Assert.assertTrue(expectedException)
            }
        }
    }

    @Test
    fun testRollbackDropCollection() {
        val collection = db!!.getCollection("test")
        collection.createIndex("firstName")
        val document = Document.createDocument("firstName", "John")
        collection.insert(document)

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    txCol.drop()
                    var expectedException = false
                    try {
                        Assert.assertEquals(0, txCol.size())
                    } catch (e: NitriteIOException) {
                        expectedException = true
                    }
                    Assert.assertTrue(expectedException)
                    Assert.assertEquals(1, collection.size())
                    throw TransactionException("failed")
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertEquals(1, collection.size())
                }
            }
        }
    }

    @Test
    fun testCommitSetAttribute() {
        val collection = db!!.getCollection("test")

        db!!.session {
            tx {
                val txCol = getCollection("test")
                val attributes = Attributes()
                val hashMap: MutableMap<String, String> =
                    HashMap()
                hashMap["key"] = "value"
                attributes.attributes = hashMap
                txCol.attributes = attributes
                Assert.assertNull(collection.attributes)
                commit()
                Assert.assertEquals("value", collection.attributes.get("key"))
            }
        }
    }

    @Test
    fun testRollbackSetAttribute() {
        val collection = db!!.getCollection("test")
        collection.createIndex("firstName")

        db!!.session {
            tx {
                try {
                    val txCol = getCollection("test")
                    val attributes = Attributes()
                    val map: MutableMap<String, String> =
                        HashMap()
                    map["key"] = "value"
                    attributes.attributes = map
                    txCol.attributes = attributes
                    txCol.insert(Document.createDocument("firstName", "John"))
                    txCol.insert(
                        Document.createDocument("firstName", "Jane").put("lastName", "Doe")
                    )
                    Assert.assertNull(collection.attributes)

                    // just to create UniqueConstraintViolation for rollback
                    collection.insert(Document.createDocument("firstName", "Jane"))
                    Assert.assertEquals(
                        txCol.find(
                            FluentFilter.where("firstName").eq("John")
                        ).size(), 1
                    )
                    Assert.assertNotEquals(
                        collection.find(
                            FluentFilter.where("lastName").eq("Doe")
                        ).size(), 1
                    )
                    commit()
                    Assert.fail()
                } catch (e: TransactionException) {
                    rollback()
                    Assert.assertNull(collection.attributes.get("key"))
                }
            }
        }
    }

    @Test
    fun testConcurrentInsertAndRemove() {
        val collection = db!!.getCollection("test")
        collection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "firstName")
        collection.createIndex("id")
        val faker = Faker()
        val futures: MutableList<Future<*>> = ArrayList()

        db!!.createSession().use { session ->
            val executorService = Executors.newCachedThreadPool()
            for (i in 0..9) {
                val fi = i.toLong()
                val future = executorService.submit {
                    val transaction = session.beginTransaction()
                    try {
                        val txCol = transaction.getCollection("test")
                        for (j in 0..9) {
                            val document =
                                Document.createDocument("firstName", faker.name().firstName())
                                    .put("lastName", faker.name().lastName())
                                    .put("id", j + fi * 10)
                            txCol.insert(document)
                        }
                        txCol.remove(FluentFilter.where("id").eq(2 + fi * 10))
                        transaction.commit()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        transaction.rollback()
                    } finally {
                        transaction.close()
                    }
                }
                futures.add(future)
            }
            futures.forEach(Consumer { future: Future<*> ->
                try {
                    future.get()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            })
            Assert.assertEquals(90, collection.size())
        }
    }

    @Test
    fun testConcurrentInsert() {
        val collection = db!!.getCollection("test")
        val faker = Faker()
        val futures: MutableList<Future<*>> = ArrayList()
        db!!.createSession().use { session ->
            val executorService = Executors.newCachedThreadPool()
            for (i in 0..9) {
                val future = executorService.submit {
                    val transaction = session.beginTransaction()
                    try {
                        val txCol = transaction.getCollection("test")
                        for (j in 0..9) {
                            val document =
                                Document.createDocument("firstName", faker.name().firstName())
                                    .put("lastName", faker.name().lastName())
                            txCol.insert(document)
                        }
                        transaction.commit()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        transaction.rollback()
                    } finally {
                        transaction.close()
                    }
                }
                futures.add(future)
            }
            futures.forEach(Consumer { future: Future<*> ->
                try {
                    future.get()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            })
            Assert.assertEquals(100, collection.size())
        }
    }

    @Test
    fun testConcurrentUpdate() {
        val collection = db!!.getCollection("test")
        for (i in 0..9) {
            val document = Document.createDocument("id", i)
            collection.insert(document)
        }
        val faker = Faker()
        val futures: MutableList<Future<*>> = ArrayList()
        db!!.createSession().use { session ->
            val executorService = Executors.newCachedThreadPool()
            for (i in 0..9) {
                val future = executorService.submit {
                    val transaction = session.beginTransaction()
                    try {
                        val txCol = transaction.getCollection("test")
                        for (j in 0..9) {
                            val document =
                                Document.createDocument("firstName", faker.name().firstName())
                                    .put("lastName", faker.name().lastName())
                                    .put("id", j)
                            txCol.update(
                                FluentFilter.where("id").eq(j),
                                document,
                                UpdateOptions.updateOptions(true)
                            )
                        }
                        transaction.commit()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        transaction.rollback()
                    } finally {
                        transaction.close()
                    }
                }
                futures.add(future)
            }
            futures.forEach(Consumer { future: Future<*> ->
                try {
                    future.get()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            })
            Assert.assertEquals(10, collection.size())
        }
    }

    @Test
    fun testTransactionOnDifferentCollections() {
        val col1 = db!!.getCollection("test1")
        val col2 = db!!.getCollection("test2")
        val col3 = db!!.getCollection("test3")
        col3.createIndex("id")
        val faker = Faker()

        db!!.createSession().use { session ->
            val transaction = session.beginTransaction()
            val test1 = transaction.getCollection("test1")
            val test2 = transaction.getCollection("test2")
            val test3 = transaction.getCollection("test3")
            for (i in 0..9) {
                var document =
                    Document.createDocument("firstName", faker.name().firstName())
                        .put("id", i)
                test1.insert(document)
                document = Document.createDocument("firstName", faker.name().firstName())
                    .put("id", i + 10)
                test2.insert(document)
                document = Document.createDocument("firstName", faker.name().firstName())
                    .put("id", i + 20)
                test3.insert(document)
            }
            Assert.assertEquals(test1.size(), 10)
            Assert.assertEquals(test2.size(), 10)
            Assert.assertEquals(test3.size(), 10)
            Assert.assertEquals(col1.size(), 0)
            Assert.assertEquals(col2.size(), 0)
            Assert.assertEquals(col3.size(), 0)
            transaction.commit()
            Assert.assertEquals(col1.size(), 10)
            Assert.assertEquals(col2.size(), 10)
            Assert.assertEquals(col3.size(), 10)
        }
        var transaction: Transaction? = null
        try {
            db!!.createSession().use { session ->
                transaction = session.beginTransaction()
                val test1 = transaction!!.getCollection("test1")
                val test2 = transaction!!.getCollection("test2")
                val test3 = transaction!!.getCollection("test3")
                for (i in 0..9) {
                    var document =
                        Document.createDocument("firstName", faker.name().firstName())
                            .put("id", i + 30)
                    test1.insert(document)
                    document = Document.createDocument("firstName", faker.name().firstName())
                        .put("id", i + 40)
                    test2.insert(document)
                    document = Document.createDocument("firstName", faker.name().firstName())
                        .put("id", i + 50)
                    test3.insert(document)
                }
                Assert.assertEquals(test1.size(), 20)
                Assert.assertEquals(test2.size(), 20)
                Assert.assertEquals(test3.size(), 20)
                Assert.assertEquals(col1.size(), 10)
                Assert.assertEquals(col2.size(), 10)
                Assert.assertEquals(col3.size(), 10)
                val document =
                    Document.createDocument("firstName", faker.name().firstName())
                        .put("id", 52)
                col3.insert(document)
                transaction!!.commit()
                Assert.fail()
            }
        } catch (e: TransactionException) {
            assert(transaction != null)
            transaction!!.rollback()
            Assert.assertEquals(col1.size(), 10)
            Assert.assertEquals(col2.size(), 10)
            Assert.assertEquals(col3.size(), 11) // last document added
        }
    }

    @Test(expected = TransactionException::class)
    fun testFailureOnClosedTransaction() {
        db!!.session {
            tx {
                val col = getCollection("test")
                col.insert(Document.createDocument("id", 1))
                commit()
                col.insert(Document.createDocument("id", 2))
                Assert.fail()
            }
        }
    }
}
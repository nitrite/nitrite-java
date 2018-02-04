package org.dizitart.kno2

import org.dizitart.kno2.tool.exportTo
import org.dizitart.kno2.tool.importFrom
import org.dizitart.no2.objects.data.Company
import org.dizitart.no2.objects.data.DataGenerator
import org.dizitart.no2.objects.data.Employee
import org.dizitart.no2.tool.BaseExternalTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.util.*

/**
 * @since 2.1.1
 * @author Anindya Chatterjee.
 */
class ImportExportTest : BaseExternalTest() {

    @Test
    fun testImportExport() {
        schemaFile = (System.getProperty("java.io.tmpdir") + File.separator
            + "nitrite" + File.separator + "schema.json")

        val random = Random()
        for (i in 0..4) {
            sourceEmpRepo.insert(DataGenerator.generateEmployee())
            sourceCompRepo.insert(DataGenerator.generateCompanyRecord())

            var document = documentOf("first-field" to random.nextGaussian())
            sourceFirstColl.insert(document)

            document = documentOf("second-field" to random.nextLong())
            sourceSecondColl.insert(document)
        }

        sourceDb exportTo schemaFile
        destDb importFrom schemaFile

        val destFirstColl = destDb.getCollection("first")
        val destSecondColl = destDb.getCollection("second")
        val destEmpRepo = destDb.getRepository(Employee::class.java)
        val destCompRepo = destDb.getRepository(Company::class.java)

        assertEquals(filter(sourceFirstColl.find().toList()),
            filter(destFirstColl!!.find().toList()))
        assertEquals(filter(sourceSecondColl.find().toList()),
            filter(destSecondColl!!.find().toList()))

        assertEquals(sourceEmpRepo.find().toList(),
            destEmpRepo!!.find().toList())
        assertEquals(sourceCompRepo.find().toList(),
            destCompRepo!!.find().toList())

        assertEquals(sourceEmpRepo.listIndices(), destEmpRepo.listIndices())
        assertEquals(sourceCompRepo.listIndices(), destCompRepo.listIndices())
        assertEquals(sourceFirstColl.listIndices(), destFirstColl.listIndices())
        assertEquals(sourceSecondColl.listIndices(), destSecondColl.listIndices())
    }
}
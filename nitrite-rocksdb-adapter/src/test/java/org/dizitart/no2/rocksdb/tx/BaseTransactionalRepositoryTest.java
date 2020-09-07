package org.dizitart.no2.rocksdb.tx;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.repository.ObjectRepository;
import org.dizitart.no2.repository.TransactionalRepository;
import org.dizitart.no2.rocksdb.DbTestOperations;
import org.dizitart.no2.rocksdb.RocksDBModule;
import org.dizitart.no2.rocksdb.repository.data.*;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.dizitart.no2.filters.Filter.ALL;
import static org.dizitart.no2.rocksdb.TestUtil.deleteFile;

/**
 * @author Anindya Chatterjee
 */
@RunWith(value = Parameterized.class)
public abstract class BaseTransactionalRepositoryTest {
    @Parameterized.Parameter
    public boolean isProtected = false;

    protected Nitrite db;
    ObjectRepository<Company> companyRepository;
    TransactionalRepository<Company> txCompanyRepository;
    ObjectRepository<Employee> employeeRepository;
    TransactionalRepository<Employee> txEmployeeRepository;

    ObjectRepository<ClassA> aObjectRepository;
    ObjectRepository<ClassC> cObjectRepository;
    private final String fileName = DbTestOperations.getRandomTempDbFile();

    @Parameterized.Parameters(name = "Protected = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {false},
            {true},
        });
    }

    @Before
    public void setUp() {
        openDb();

        companyRepository = db.getRepository(Company.class);
        employeeRepository = db.getRepository(Employee.class);

        txCompanyRepository = companyRepository.beginTransaction();
        txEmployeeRepository = employeeRepository.beginTransaction();

        aObjectRepository = db.getRepository(ClassA.class);
        cObjectRepository = db.getRepository(ClassC.class);

        for (int i = 0; i < 10; i++) {
            Company company = DataGenerator.generateCompanyRecord();
            txCompanyRepository.insert(company);
            Employee employee = DataGenerator.generateEmployee();
            employee.setEmpId((long) i + 1);
            txEmployeeRepository.insert(employee);

            aObjectRepository.insert(ClassA.create(i + 50));
            cObjectRepository.insert(ClassC.create(i + 30));
        }
    }

    protected void openDb() {
        RocksDBModule storeModule = RocksDBModule.withConfig()
            .filePath(fileName)
            .build();

        NitriteBuilder nitriteBuilder = Nitrite.builder()
            .fieldSeparator(".")
            .loadModule(storeModule);

        if (isProtected) {
            db = nitriteBuilder.openOrCreate("test-user", "test-password");
        } else {
            db = nitriteBuilder.openOrCreate();
        }
    }

    @After
    public void clear() throws IOException {
        txEmployeeRepository.close();
        txCompanyRepository.close();

        if (companyRepository != null && !companyRepository.isDropped()) {
            companyRepository.remove(ALL);
        }

        if (employeeRepository != null && !employeeRepository.isDropped()) {
            employeeRepository.remove(ALL);
        }

        if (aObjectRepository != null && !aObjectRepository.isDropped()) {
            aObjectRepository.remove(ALL);
        }

        if (cObjectRepository != null && !cObjectRepository.isDropped()) {
            cObjectRepository.remove(ALL);
        }

        if (db != null && !db.isClosed()) {
            db.commit();
            db.close();
        }

        deleteFile(fileName);
    }
}

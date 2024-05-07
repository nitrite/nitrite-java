package org.dizitart.no2.migration;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.store.StoreMetaData;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Queue;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class MigrationManagerTest {

    @Test
    @SuppressWarnings("unchecked")
    public void findMigrationPathTestWhenStartAndEndAreSame() throws Exception {
        NitriteConfig nitriteConfigMock = Mockito.mock(NitriteConfig.class);
        StoreMetaData storeMetaDataMock = Mockito.mock(StoreMetaData.class);

        Nitrite nitriteMock = Mockito.mock(Nitrite.class);
        when(nitriteMock.getConfig()).thenReturn(nitriteConfigMock);

        MigrationManager migrationManager = new MigrationManager(nitriteMock);

        when(nitriteMock.getDatabaseMetaData()).thenReturn(storeMetaDataMock);

        Method method = MigrationManager.class.getDeclaredMethod("findMigrationPath", int.class, int.class);
        method.setAccessible(true);
        var result = (Queue<Migration>) method.invoke(migrationManager, 1, 1);

        assertNotNull(result);
    }
}

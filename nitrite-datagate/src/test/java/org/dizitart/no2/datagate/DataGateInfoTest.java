package org.dizitart.no2.datagate;

import org.dizitart.no2.sync.data.InfoResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.dizitart.no2.datagate.Constants.STORAGE_VENDOR;
import static org.dizitart.no2.datagate.Constants.VENDOR;
import static org.dizitart.no2.datagate.Constants.VERSION;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anindya Chatterjee.
 */
@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DataGateInfoTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testInfo() {
        ResponseEntity<InfoResponse> responseEntity =
            restTemplate.getForEntity("/api/v1/", InfoResponse.class);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity.getBody().getVendor(), VENDOR);
        assertNotNull(responseEntity.getBody().getVendor(), VERSION);
        assertNotNull(responseEntity.getBody().getStorage().getVendor(), STORAGE_VENDOR);
    }
}

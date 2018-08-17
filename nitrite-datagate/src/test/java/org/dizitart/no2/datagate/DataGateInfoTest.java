/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.datagate;

import org.dizitart.no2.sync.types.InfoResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.dizitart.no2.datagate.Constants.*;
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

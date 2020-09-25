package org.dizitart.no2.sync.net;

import org.dizitart.no2.sync.Config;
import org.dizitart.no2.sync.TimeSpan;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class DataGateSocketTest {
    @Test
    public void testConstructor() {
        TimeSpan timeSpan = new TimeSpan(10L, TimeUnit.NANOSECONDS);
        Config config = new Config();
        config.setTimeout(timeSpan);
        new DataGateSocket(config);
        assertNull(config.getChunkSize());
        assertNull(config.getDebounce());
        assertFalse(config.isAcceptAllCertificates());
        assertNull(config.getObjectMapper());
        assertNull(config.getRequestBuilder());
        assertNull(config.getProxy());
        assertSame(timeSpan, config.getTimeout());
        assertNull(config.getCollection());
        assertNull(config.getAuthToken());
        assertNull(config.getUserName());
        assertEquals("Config(collection=null, chunkSize=null, userName=null, debounce=null, objectMapper=null, timeout"
            + "=TimeSpan(time=10, timeUnit=NANOSECONDS), requestBuilder=null, proxy=null, authToken=null, acceptAll"
            + "Certificates=false, networkConnectivityChecker=null)", config.toString());
        assertNull(config.getNetworkConnectivityChecker());
    }

    @Test
    public void testConstructor2() {
        TimeSpan timeSpan = new TimeSpan(-1L, TimeUnit.NANOSECONDS);
        Config config = new Config();
        config.setTimeout(timeSpan);
        new DataGateSocket(config);
        assertNull(config.getChunkSize());
        assertNull(config.getDebounce());
        assertFalse(config.isAcceptAllCertificates());
        assertNull(config.getObjectMapper());
        assertNull(config.getRequestBuilder());
        assertNull(config.getProxy());
        assertSame(timeSpan, config.getTimeout());
        assertNull(config.getCollection());
        assertNull(config.getAuthToken());
        assertNull(config.getUserName());
        assertEquals("Config(collection=null, chunkSize=null, userName=null, debounce=null, objectMapper=null, timeout"
            + "=TimeSpan(time=-1, timeUnit=NANOSECONDS), requestBuilder=null, proxy=null, authToken=null, acceptAll"
            + "Certificates=false, networkConnectivityChecker=null)", config.toString());
        assertNull(config.getNetworkConnectivityChecker());
    }

    @Test
    public void testConstructor3() {
        TimeSpan timeSpan = new TimeSpan(0L, TimeUnit.NANOSECONDS);
        timeSpan.setTime(10L);
        Config config = new Config();
        config.setTimeout(timeSpan);
        new DataGateSocket(config);
        assertNull(config.getChunkSize());
        assertNull(config.getDebounce());
        assertFalse(config.isAcceptAllCertificates());
        assertNull(config.getObjectMapper());
        assertNull(config.getRequestBuilder());
        assertNull(config.getProxy());
        assertSame(timeSpan, config.getTimeout());
        assertNull(config.getCollection());
        assertNull(config.getAuthToken());
        assertNull(config.getUserName());
        assertEquals("Config(collection=null, chunkSize=null, userName=null, debounce=null, objectMapper=null, timeout"
            + "=TimeSpan(time=10, timeUnit=NANOSECONDS), requestBuilder=null, proxy=null, authToken=null, acceptAll"
            + "Certificates=false, networkConnectivityChecker=null)", config.toString());
        assertNull(config.getNetworkConnectivityChecker());
    }
}


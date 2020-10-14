package org.dizitart.no2.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Request;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConfigTest {
    @Test
    public void testCanEqual() {
        assertFalse((new Config()).canEqual("other"));
    }

    @Test
    public void testEquals() {
        Config config = new Config();
        config.setChunkSize(3);
        assertFalse((new Config()).equals(config));
    }

    @Test
    public void testEquals10() {
        Config config = new Config();
        config.setChunkSize(3);
        assertFalse(config.equals(new Config()));
    }

    @Test
    public void testEquals11() {
        Config config = new Config();
        config.setUserName("janedoe");
        assertFalse((new Config()).equals(config));
    }

    @Test
    public void testEquals12() {
        Config config = new Config();
        config.setDebounce(0);
        assertFalse(config.equals(new Config()));
    }

    @Test
    public void testEquals13() {
        Config config = new Config();
        config.setDebounce(0);
        assertFalse((new Config()).equals(config));
    }

    @Test
    public void testEquals14() {
        Config config = new Config();
        config.setObjectMapper(new ObjectMapper());
        assertFalse(config.equals(new Config()));
    }

    @Test
    public void testEquals15() {
        Config config = new Config();
        config.setUserName("janedoe");
        assertFalse(config.equals(new Config()));
    }

    @Test
    public void testEquals2() {
        Config config = new Config();
        config.setAuthToken("ABC123");
        assertFalse((new Config()).equals(config));
    }

    @Test
    public void testEquals3() {
        Config config = new Config();
        config.setObjectMapper(new ObjectMapper());
        assertFalse((new Config()).equals(config));
    }

    @Test
    public void testEquals4() {
        Config config = new Config();
        config.setAuthToken("ABC123");
        assertFalse(config.equals(new Config()));
    }

    @Test
    public void testEquals5() {
        assertFalse((new Config()).equals("o"));
    }

    @Test
    public void testEquals6() {
        Config config = new Config();
        assertTrue(config.equals(new Config()));
    }

    @Test
    public void testEquals7() {
        Config config = new Config();
        config.setAcceptAllCertificates(true);
        assertFalse(config.equals(new Config()));
    }

    @Test
    public void testEquals8() {
        TimeSpan timeout = new TimeSpan(10L, TimeUnit.NANOSECONDS);
        Config config = new Config();
        config.setTimeout(timeout);
        assertFalse(config.equals(new Config()));
    }

    @Test
    public void testEquals9() {
        Config config = new Config();
        config.setRequestBuilder(new Request.Builder());
        assertFalse(config.equals(new Config()));
    }

    @Test
    public void testSetAcceptAllCertificates() {
        Config config = new Config();
        config.setAcceptAllCertificates(true);
        assertTrue(config.isAcceptAllCertificates());
    }

    @Test
    public void testSetAuthToken() {
        Config config = new Config();
        config.setAuthToken("ABC123");
        assertEquals("ABC123", config.getAuthToken());
    }

    @Test
    public void testSetChunkSize() {
        Config config = new Config();
        config.setChunkSize(3);
        assertEquals(3, config.getChunkSize().intValue());
    }

    @Test
    public void testSetCollection() {
        Config config = new Config();
        config.setCollection(null);
        assertNull(config.getCollection());
    }

    @Test
    public void testSetDebounce() {
        Config config = new Config();
        config.setDebounce(1);
        assertEquals(1, config.getDebounce().intValue());
    }

    @Test
    public void testSetProxy() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1));
        Config config = new Config();
        config.setProxy(proxy);
        assertEquals(
            "Config(collection=null, chunkSize=null, userName=null, debounce=null, objectMapper=null, timeout=null,"
                + " requestBuilder=null, proxy=HTTP @ 0.0.0.0/0.0.0.0:1, authToken=null, acceptAllCertificates=false,"
                + " networkConnectivityChecker=null)",
            config.toString());
    }

    @Test
    public void testSetRequestBuilder() {
        Config config = new Config();
        Request.Builder builder = new Request.Builder();
        config.setRequestBuilder(builder);
        assertSame(builder, config.getRequestBuilder());
    }

    @Test
    public void testSetTimeout() {
        TimeSpan timeout = new TimeSpan(10L, TimeUnit.NANOSECONDS);
        Config config = new Config();
        config.setTimeout(timeout);
        assertEquals("Config(collection=null, chunkSize=null, userName=null, debounce=null, objectMapper=null, timeout"
            + "=TimeSpan(time=10, timeUnit=NANOSECONDS), requestBuilder=null, proxy=null, authToken=null, acceptAll"
            + "Certificates=false, networkConnectivityChecker=null)", config.toString());
    }

    @Test
    public void testSetUserName() {
        Config config = new Config();
        config.setUserName("janedoe");
        assertEquals("janedoe", config.getUserName());
    }

    @Test
    public void testToString() {
        assertEquals(
            "Config(collection=null, chunkSize=null, userName=null, debounce=null, objectMapper=null, timeout=null,"
                + " requestBuilder=null, proxy=null, authToken=null, acceptAllCertificates=false, networkConnectivityChecker"
                + "=null)",
            (new Config()).toString());
    }
}


/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.dizitart.no2.mvstore.compat.v1.mvstore;

import org.h2.util.MathUtils;
import org.h2.util.Utils;

import java.io.File;

public class SysProperties {
    public static final String H2_SCRIPT_DIRECTORY = "h2.scriptDirectory";
    public static final String H2_BROWSER = "h2.browser";
    public static final String FILE_SEPARATOR;
    public static final String LINE_SEPARATOR;
    public static final String USER_HOME;
    public static final boolean PREVIEW;
    public static final String ALLOWED_CLASSES;
    public static final boolean ENABLE_ANONYMOUS_TLS;
    public static final String BIND_ADDRESS;
    public static final boolean CHECK;
    public static final String CLIENT_TRACE_DIRECTORY;
    public static final int COLLATOR_CACHE_SIZE;
    public static final int CONSOLE_MAX_TABLES_LIST_INDEXES;
    public static final int CONSOLE_MAX_TABLES_LIST_COLUMNS;
    public static final int CONSOLE_MAX_PROCEDURES_LIST_COLUMNS;
    public static final boolean CONSOLE_STREAM;
    public static final int CONSOLE_TIMEOUT;
    public static final int DATASOURCE_TRACE_LEVEL;
    public static final int DELAY_WRONG_PASSWORD_MIN;
    public static final int DELAY_WRONG_PASSWORD_MAX;
    public static final boolean JAVA_SYSTEM_COMPILER;
    public static boolean lobCloseBetweenReads;
    public static final int LOB_FILES_PER_DIRECTORY;
    public static final int LOB_CLIENT_MAX_SIZE_MEMORY;
    public static final int MAX_FILE_RETRY;
    public static final int MAX_RECONNECT;
    public static final int MAX_MEMORY_ROWS;
    public static final long MAX_TRACE_DATA_LENGTH;
    public static final boolean MODIFY_ON_WRITE;
    public static final boolean NIO_LOAD_MAPPED;
    public static final boolean NIO_CLEANER_HACK;
    public static final boolean OBJECT_CACHE;
    public static final int OBJECT_CACHE_MAX_PER_ELEMENT_SIZE;
    public static final int OBJECT_CACHE_SIZE;
    public static final boolean OLD_RESULT_SET_GET_OBJECT;
    public static final boolean BIG_DECIMAL_IS_DECIMAL;
    public static final boolean RETURN_OFFSET_DATE_TIME;
    public static final String PG_DEFAULT_CLIENT_ENCODING;
    public static final String PREFIX_TEMP_FILE;
    public static boolean FORCE_AUTOCOMMIT_OFF_ON_COMMIT;
    public static final int SERVER_CACHED_OBJECTS;
    public static final int SERVER_RESULT_SET_FETCH_SIZE;
    public static final int SOCKET_CONNECT_RETRY;
    public static final int SOCKET_CONNECT_TIMEOUT;
    public static final boolean SORT_BINARY_UNSIGNED;
    public static final boolean SORT_UUID_UNSIGNED;
    public static final boolean SORT_NULLS_HIGH;
    public static final long SPLIT_FILE_SIZE_SHIFT;
    public static final String SYNC_METHOD;
    public static final boolean TRACE_IO;
    public static final boolean THREAD_DEADLOCK_DETECTOR;
    public static final boolean IMPLICIT_RELATIVE_PATH;
    public static final String URL_MAP;
    public static final boolean USE_THREAD_CONTEXT_CLASS_LOADER;
    public static boolean serializeJavaObject;
    public static final String JAVA_OBJECT_SERIALIZER;
    public static final String CUSTOM_DATA_TYPES_HANDLER;
    public static final String AUTH_CONFIG_FILE;
    private static final String H2_BASE_DIR = "h2.baseDir";

    private SysProperties() {
    }

    public static void setBaseDir(String var0) {
        if (!var0.endsWith("/")) {
            var0 = var0 + "/";
        }

        System.setProperty("h2.baseDir", var0);
    }

    public static String getBaseDir() {
        return Utils.getProperty("h2.baseDir", (String)null);
    }

    public static String getScriptDirectory() {
        return Utils.getProperty("h2.scriptDirectory", "");
    }

    private static int getAutoScaledForMemoryProperty(String var0, int var1) {
        String var2 = Utils.getProperty(var0, (String)null);
        if (var2 != null) {
            try {
                return Integer.decode(var2);
            } catch (NumberFormatException var4) {
            }
        }

        return Utils.scaleForAvailableMemory(var1);
    }

    static {
        FILE_SEPARATOR = File.separator;
        LINE_SEPARATOR = System.lineSeparator();
        USER_HOME = Utils.getProperty("user.home", "");
        PREVIEW = Utils.getProperty("h2.preview", false);
        ALLOWED_CLASSES = Utils.getProperty("h2.allowedClasses", "*");
        ENABLE_ANONYMOUS_TLS = Utils.getProperty("h2.enableAnonymousTLS", true);
        BIND_ADDRESS = Utils.getProperty("h2.bindAddress", (String)null);
        CHECK = Utils.getProperty("h2.check", !"0.9".equals(Utils.getProperty("java.specification.version", (String)null)));
        CLIENT_TRACE_DIRECTORY = Utils.getProperty("h2.clientTraceDirectory", "trace.db/");
        COLLATOR_CACHE_SIZE = Utils.getProperty("h2.collatorCacheSize", 32000);
        CONSOLE_MAX_TABLES_LIST_INDEXES = Utils.getProperty("h2.consoleTableIndexes", 100);
        CONSOLE_MAX_TABLES_LIST_COLUMNS = Utils.getProperty("h2.consoleTableColumns", 500);
        CONSOLE_MAX_PROCEDURES_LIST_COLUMNS = Utils.getProperty("h2.consoleProcedureColumns", 300);
        CONSOLE_STREAM = Utils.getProperty("h2.consoleStream", true);
        CONSOLE_TIMEOUT = Utils.getProperty("h2.consoleTimeout", 1800000);
        DATASOURCE_TRACE_LEVEL = Utils.getProperty("h2.dataSourceTraceLevel", 1);
        DELAY_WRONG_PASSWORD_MIN = Utils.getProperty("h2.delayWrongPasswordMin", 250);
        DELAY_WRONG_PASSWORD_MAX = Utils.getProperty("h2.delayWrongPasswordMax", 4000);
        JAVA_SYSTEM_COMPILER = Utils.getProperty("h2.javaSystemCompiler", true);
        lobCloseBetweenReads = Utils.getProperty("h2.lobCloseBetweenReads", false);
        LOB_FILES_PER_DIRECTORY = Utils.getProperty("h2.lobFilesPerDirectory", 256);
        LOB_CLIENT_MAX_SIZE_MEMORY = Utils.getProperty("h2.lobClientMaxSizeMemory", 1048576);
        MAX_FILE_RETRY = Math.max(1, Utils.getProperty("h2.maxFileRetry", 16));
        MAX_RECONNECT = Utils.getProperty("h2.maxReconnect", 3);
        MAX_MEMORY_ROWS = getAutoScaledForMemoryProperty("h2.maxMemoryRows", 40000);
        MAX_TRACE_DATA_LENGTH = (long)Utils.getProperty("h2.maxTraceDataLength", 65535);
        MODIFY_ON_WRITE = Utils.getProperty("h2.modifyOnWrite", false);
        NIO_LOAD_MAPPED = Utils.getProperty("h2.nioLoadMapped", false);
        NIO_CLEANER_HACK = Utils.getProperty("h2.nioCleanerHack", false);
        OBJECT_CACHE = Utils.getProperty("h2.objectCache", true);
        OBJECT_CACHE_MAX_PER_ELEMENT_SIZE = Utils.getProperty("h2.objectCacheMaxPerElementSize", 4096);

        try {
            OBJECT_CACHE_SIZE = MathUtils.nextPowerOf2(Utils.getProperty("h2.objectCacheSize", 1024));
        } catch (IllegalArgumentException var1) {
            throw new IllegalStateException("Invalid h2.objectCacheSize", var1);
        }

        OLD_RESULT_SET_GET_OBJECT = Utils.getProperty("h2.oldResultSetGetObject", !PREVIEW);
        BIG_DECIMAL_IS_DECIMAL = Utils.getProperty("h2.bigDecimalIsDecimal", !PREVIEW);
        RETURN_OFFSET_DATE_TIME = Utils.getProperty("h2.returnOffsetDateTime", PREVIEW);
        PG_DEFAULT_CLIENT_ENCODING = Utils.getProperty("h2.pgClientEncoding", "UTF-8");
        PREFIX_TEMP_FILE = Utils.getProperty("h2.prefixTempFile", "h2.temp");
        FORCE_AUTOCOMMIT_OFF_ON_COMMIT = Utils.getProperty("h2.forceAutoCommitOffOnCommit", false);
        SERVER_CACHED_OBJECTS = Utils.getProperty("h2.serverCachedObjects", 64);
        SERVER_RESULT_SET_FETCH_SIZE = Utils.getProperty("h2.serverResultSetFetchSize", 100);
        SOCKET_CONNECT_RETRY = Utils.getProperty("h2.socketConnectRetry", 16);
        SOCKET_CONNECT_TIMEOUT = Utils.getProperty("h2.socketConnectTimeout", 2000);
        SORT_BINARY_UNSIGNED = Utils.getProperty("h2.sortBinaryUnsigned", true);
        SORT_UUID_UNSIGNED = Utils.getProperty("h2.sortUuidUnsigned", PREVIEW);
        SORT_NULLS_HIGH = Utils.getProperty("h2.sortNullsHigh", false);
        SPLIT_FILE_SIZE_SHIFT = (long)Utils.getProperty("h2.splitFileSizeShift", 30);
        SYNC_METHOD = Utils.getProperty("h2.syncMethod", "sync");
        TRACE_IO = Utils.getProperty("h2.traceIO", false);
        THREAD_DEADLOCK_DETECTOR = Utils.getProperty("h2.threadDeadlockDetector", false);
        IMPLICIT_RELATIVE_PATH = Utils.getProperty("h2.implicitRelativePath", false);
        URL_MAP = Utils.getProperty("h2.urlMap", (String)null);
        USE_THREAD_CONTEXT_CLASS_LOADER = Utils.getProperty("h2.useThreadContextClassLoader", false);
        serializeJavaObject = Utils.getProperty("h2.serializeJavaObject", true);
        JAVA_OBJECT_SERIALIZER = Utils.getProperty("h2.javaObjectSerializer", (String)null);
        CUSTOM_DATA_TYPES_HANDLER = Utils.getProperty("h2.customDataTypesHandler", (String)null);
        AUTH_CONFIG_FILE = Utils.getProperty("h2.authConfigFile", (String)null);
    }
}

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

package org.dizitart.no2.tool;

import org.h2.mvstore.Chunk;
import org.h2.mvstore.DataUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.store.fs.FilePath;
import org.h2.store.fs.FileUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.notEmpty;
import static org.dizitart.no2.util.ValidationUtils.notNull;
import static org.h2.mvstore.Chunk.fromString;

/**
 * The nitrite database recovery utility.
 *
 * @since 1.0
 * @author H2 Group
 * @author Anindya Chatterjee
 */
public class Recovery {
    /**
     * The block size (physical sector size) of the disk. The store header is
     * written twice, one copy in each block, to ensure it survives a crash.
     */
    private static final int BLOCK_SIZE = 4 * 1024;

    /**
     * The maximum length of a chunk header, in bytes.
     */
    private static final int MAX_HEADER_LENGTH = 1024;

    /**
     * Attempt a database file recovery by rolling back to the
     * newest good version.
     *
     * @param fileName the database file name
     * @return `true` if repair successful; otherwise `false`
     */
    public static boolean recover(String fileName) {
        return recover(fileName, new PrintWriter(System.out));
    }

    /**
     * Attempt a database file recovery by rolling back to the
     * newest good version. Writes the log using the specified writer.
     *
     * @param fileName the database file name
     * @param writer   the log writer
     * @return `true` if repair successful; otherwise `false`
     */
    public static boolean recover(String fileName, PrintWriter writer) {
        notNull(fileName, errorMessage("fileName can not be null", VE_RECOVER_NULL_FILE_NAME));
        notEmpty(fileName, errorMessage("fileName can not be empty", VE_RECOVER_EMPTY_FILE_NAME));
        notNull(writer, errorMessage("writer can not be null", VE_RECOVER_NULL_WRITER));
        return repair(fileName, writer);
    }

    /**
     * Repair a store by rolling back to the newest good version.
     *
     * @param fileName the file name
     */
    private static boolean repair(String fileName, PrintWriter pw) {
        long version = Long.MAX_VALUE;
        OutputStream ignore = new OutputStream() {
            @Override
            public void write(int b) {
                // ignore
            }
        };
        boolean repaired = false;
        while (version >= 0) {
            pw.println(version == Long.MAX_VALUE ? "Trying latest version" : ("Trying version " + version));
            pw.flush();
            version = rollback(fileName, version, new PrintWriter(ignore));
            try {
                String error = info(fileName + ".temp", new PrintWriter(ignore));
                if (error == null) {
                    FilePath.get(fileName).moveTo(FilePath.get(fileName + ".back"), true);
                    FilePath.get(fileName + ".temp").moveTo(FilePath.get(fileName), true);
                    pw.println("Success");
                    repaired = true;
                    break;
                }
                pw.println("    ... failed: " + error);
            } catch (Exception e) {
                pw.println("Fail: " + e.getMessage());
                pw.flush();
            }
            version--;
        }
        pw.flush();
        return repaired;
    }

    /**
     * Roll back to a given revision into a a file called *.temp.
     *
     * @param fileName the file name
     * @param targetVersion the version to roll back to (Long.MAX_VALUE for the latest version)
     * @return the version rolled back to (-1 if no version)
     */
    private static long rollback(String fileName, long targetVersion, Writer writer) {
        long newestVersion = -1;
        PrintWriter pw = new PrintWriter(writer, true);
        if (!FilePath.get(fileName).exists()) {
            pw.println("File not found: " + fileName);
            return newestVersion;
        }
        FileChannel file = null;
        FileChannel target = null;
        int blockSize = BLOCK_SIZE;
        try {
            file = FilePath.get(fileName).open("r");
            FilePath.get(fileName + ".temp").delete();
            target = FilePath.get(fileName + ".temp").open("rw");
            long fileSize = file.size();
            ByteBuffer block = ByteBuffer.allocate(4096);
            Chunk newestChunk = null;
            for (long pos = 0; pos < fileSize;) {
                block.rewind();
                DataUtils.readFully(file, pos, block);
                block.rewind();
                int headerType = block.get();
                if (headerType == 'H') {
                    block.rewind();
                    target.write(block, pos);
                    pos += blockSize;
                    continue;
                }
                if (headerType != 'c') {
                    pos += blockSize;
                    continue;
                }
                Chunk c;
                try {
                    c = readChunkHeader(block, pos);
                } catch (IllegalStateException e) {
                    pos += blockSize;
                    continue;
                }
                if (c.len <= 0) {
                    // not a chunk
                    pos += blockSize;
                    continue;
                }
                int length = c.len * BLOCK_SIZE;
                ByteBuffer chunk = ByteBuffer.allocate(length);
                DataUtils.readFully(file, pos, chunk);
                if (c.version > targetVersion) {
                    // newer than the requested version
                    pos += length;
                    continue;
                }
                chunk.rewind();
                target.write(chunk, pos);
                if (newestChunk == null || c.version > newestChunk.version) {
                    newestChunk = c;
                    newestVersion = c.version;
                }
                pos += length;
            }
            if (newestChunk != null) {
                int length = newestChunk.len * BLOCK_SIZE;
                ByteBuffer chunk = ByteBuffer.allocate(length);
                DataUtils.readFully(file, newestChunk.block * BLOCK_SIZE, chunk);
                chunk.rewind();
                target.write(chunk, fileSize);
            }
        } catch (IOException e) {
            pw.println("ERROR: " + e);
            e.printStackTrace(pw);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (target != null) {
                try {
                    target.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        pw.flush();
        return newestVersion;
    }

    /**
     * Read the header from the byte buffer.
     *
     * @param buff the source buffer
     * @param start the start of the chunk in the file
     * @return the chunk
     */
    private static Chunk readChunkHeader(ByteBuffer buff, long start) {
        int pos = buff.position();
        byte[] data = new byte[Math.min(buff.remaining(), MAX_HEADER_LENGTH)];
        buff.get(data);
        try {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == '\n') {
                    // set the position to the start of the first page
                    buff.position(pos + i + 1);
                    String s = new String(data, 0, i, Charset.forName("ISO-8859-1")).trim();
                    return fromString(s);
                }
            }
        } catch (Exception e) {
            // there could be various reasons
            throw DataUtils.newIllegalStateException(
                    DataUtils.ERROR_FILE_CORRUPT,
                    "File corrupt reading chunk at position {0}", start, e);
        }
        throw DataUtils.newIllegalStateException(
                DataUtils.ERROR_FILE_CORRUPT,
                "File corrupt reading chunk at position {0}", start);
    }

    /**
     * Read the summary information of the file and write them to system out.
     *
     * @param fileName the name of the file.
     * @param writer the print writer.
     * @return null if successful (if there was no error), otherwise the error message
     */
    private static String info(String fileName, Writer writer) {
        PrintWriter pw = new PrintWriter(writer, true);
        if (!FilePath.get(fileName).exists()) {
            pw.println("File not found: " + fileName);
            return "File not found: " + fileName;
        }
        long fileLength = FileUtils.size(fileName);
        MVStore store = new MVStore.Builder().
                fileName(fileName).
                readOnly().open();
        try {
            MVMap<String, String> meta = store.getMetaMap();
            Map<String, Object> header = store.getStoreHeader();
            long fileCreated = DataUtils.readHexLong(header, "created", 0L);
            TreeMap<Integer, Chunk> chunks = new TreeMap<>();
            long chunkLength = 0;
            long maxLength = 0;
            long maxLengthLive = 0;
            long maxLengthNotEmpty = 0;
            for (Map.Entry<String, String> e : meta.entrySet()) {
                String k = e.getKey();
                if (k.startsWith("chunk.")) {
                    Chunk c = Chunk.fromString(e.getValue());
                    chunks.put(c.id, c);
                    chunkLength += c.len * BLOCK_SIZE;
                    maxLength += c.maxLen;
                    maxLengthLive += c.maxLenLive;
                    if (c.maxLenLive > 0) {
                        maxLengthNotEmpty += c.maxLen;
                    }
                }
            }
            pw.printf("Created: %s\n", formatTimestamp(fileCreated, fileCreated));
            pw.printf("Last modified: %s\n",
                    formatTimestamp(FileUtils.lastModified(fileName), fileCreated));
            pw.printf("File length: %d\n", fileLength);
            pw.printf("The last chunk is not listed\n");
            pw.printf("Chunk length: %d\n", chunkLength);
            pw.printf("Chunk count: %d\n", chunks.size());
            pw.printf("Used space: %d%%\n", getPercent(chunkLength, fileLength));
            pw.printf("Chunk fill rate: %d%%\n", maxLength == 0 ? 100 :
                    getPercent(maxLengthLive, maxLength));
            pw.printf("Chunk fill rate excluding empty chunks: %d%%\n",
                    maxLengthNotEmpty == 0 ? 100 :
                            getPercent(maxLengthLive, maxLengthNotEmpty));
            for (Map.Entry<Integer, Chunk> e : chunks.entrySet()) {
                Chunk c = e.getValue();
                long created = fileCreated + c.time;
                pw.printf("  Chunk %d: %s, %d%% used, %d blocks",
                        c.id, formatTimestamp(created, fileCreated),
                        getPercent(c.maxLenLive, c.maxLen),
                        c.len
                );
                if (c.maxLenLive == 0) {
                    pw.printf(", unused: %s",
                            formatTimestamp(fileCreated + c.unused, fileCreated));
                }
                pw.printf("\n");
            }
            pw.printf("\n");
        } catch (Exception e) {
            pw.println("ERROR: " + e);
            e.printStackTrace(pw);
            return e.getMessage();
        } finally {
            store.close();
        }
        pw.flush();
        return null;
    }

    private static String formatTimestamp(long t, long start) {
        String x = new Timestamp(t).toString();
        String s = x.substring(0, 19);
        s += " (+" + ((t - start) / 1000) + " s)";
        return s;
    }

    private static int getPercent(long value, long max) {
        if (value == 0) {
            return 0;
        } else if (value == max) {
            return 100;
        }
        return (int) (1 + 98 * value / Math.max(1, max));
    }
}

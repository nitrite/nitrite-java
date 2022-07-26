/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import org.h2.security.AES;
import org.h2.security.BlockCipher;
import org.h2.security.SHA256;
import org.h2.util.MathUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FilePathEncrypt extends FilePathWrapper {
    private static final String SCHEME = "encrypt";

    public FilePathEncrypt() {
    }

    public static void register() {
        FilePath.register(new FilePathEncrypt());
    }

    public FileChannel open(String var1) throws IOException {
        String[] var2 = this.parse(this.name);
        FileChannel var3 = FileUtils.open(var2[1], var1);
        byte[] var4 = var2[0].getBytes(StandardCharsets.UTF_8);
        return new FileEncrypt(this.name, var4, var3);
    }

    public String getScheme() {
        return "encrypt";
    }

    protected String getPrefix() {
        String[] var1 = this.parse(this.name);
        return this.getScheme() + ":" + var1[0] + ":";
    }

    public FilePath unwrap(String var1) {
        return FilePath.get(this.parse(var1)[1]);
    }

    public long size() {
        long var1 = this.getBase().size() - 4096L;
        var1 = Math.max(0L, var1);
        if ((var1 & 4095L) != 0L) {
            var1 -= 4096L;
        }

        return var1;
    }

    public OutputStream newOutputStream(boolean var1) throws IOException {
        return new FileChannelOutputStream(this.open("rw"), var1);
    }

    public InputStream newInputStream() throws IOException {
        return new FileChannelInputStream(this.open("r"), true);
    }

    private String[] parse(String var1) {
        if (!var1.startsWith(this.getScheme())) {
            throw new IllegalArgumentException(var1 + " doesn't start with " + this.getScheme());
        } else {
            var1 = var1.substring(this.getScheme().length() + 1);
            int var2 = var1.indexOf(58);
            if (var2 < 0) {
                throw new IllegalArgumentException(var1 + " doesn't contain encryption algorithm and password");
            } else {
                String var3 = var1.substring(0, var2);
                var1 = var1.substring(var2 + 1);
                return new String[]{var3, var1};
            }
        }
    }

    public static byte[] getPasswordBytes(char[] var0) {
        int var1 = var0.length;
        byte[] var2 = new byte[var1 * 2];

        for(int var3 = 0; var3 < var1; ++var3) {
            char var4 = var0[var3];
            var2[var3 + var3] = (byte)(var4 >>> 8);
            var2[var3 + var3 + 1] = (byte)var4;
        }

        return var2;
    }

    static class XTS {
        private static final int GF_128_FEEDBACK = 135;
        private static final int CIPHER_BLOCK_SIZE = 16;
        private final BlockCipher cipher;

        XTS(BlockCipher var1) {
            this.cipher = var1;
        }

        void encrypt(long var1, int var3, byte[] var4, int var5) {
            byte[] var6 = this.initTweak(var1);

            int var7;
            for(var7 = 0; var7 + 16 <= var3; var7 += 16) {
                if (var7 > 0) {
                    updateTweak(var6);
                }

                xorTweak(var4, var7 + var5, var6);
                this.cipher.encrypt(var4, var7 + var5, 16);
                xorTweak(var4, var7 + var5, var6);
            }

            if (var7 < var3) {
                updateTweak(var6);
                swap(var4, var7 + var5, var7 - 16 + var5, var3 - var7);
                xorTweak(var4, var7 - 16 + var5, var6);
                this.cipher.encrypt(var4, var7 - 16 + var5, 16);
                xorTweak(var4, var7 - 16 + var5, var6);
            }

        }

        void decrypt(long var1, int var3, byte[] var4, int var5) {
            byte[] var6 = this.initTweak(var1);
            byte[] var7 = var6;

            int var8;
            for(var8 = 0; var8 + 16 <= var3; var8 += 16) {
                if (var8 > 0) {
                    updateTweak(var6);
                    if (var8 + 16 + 16 > var3 && var8 + 16 < var3) {
                        var7 = (byte[])var6.clone();
                        updateTweak(var6);
                    }
                }

                xorTweak(var4, var8 + var5, var6);
                this.cipher.decrypt(var4, var8 + var5, 16);
                xorTweak(var4, var8 + var5, var6);
            }

            if (var8 < var3) {
                swap(var4, var8, var8 - 16 + var5, var3 - var8 + var5);
                xorTweak(var4, var8 - 16 + var5, var7);
                this.cipher.decrypt(var4, var8 - 16 + var5, 16);
                xorTweak(var4, var8 - 16 + var5, var7);
            }

        }

        private byte[] initTweak(long var1) {
            byte[] var3 = new byte[16];

            for(int var4 = 0; var4 < 16; var1 >>>= 8) {
                var3[var4] = (byte)((int)(var1 & 255L));
                ++var4;
            }

            this.cipher.encrypt(var3, 0, 16);
            return var3;
        }

        private static void xorTweak(byte[] var0, int var1, byte[] var2) {
            for(int var3 = 0; var3 < 16; ++var3) {
                var0[var1 + var3] ^= var2[var3];
            }

        }

        private static void updateTweak(byte[] var0) {
            byte var1 = 0;
            byte var2 = 0;

            for(int var3 = 0; var3 < 16; ++var3) {
                var2 = (byte)(var0[var3] >> 7 & 1);
                var0[var3] = (byte)((var0[var3] << 1) + var1 & 255);
                var1 = var2;
            }

            if (var2 != 0) {
                var0[0] = (byte)(var0[0] ^ 135);
            }

        }

        private static void swap(byte[] var0, int var1, int var2, int var3) {
            for(int var4 = 0; var4 < var3; ++var4) {
                byte var5 = var0[var1 + var4];
                var0[var1 + var4] = var0[var2 + var4];
                var0[var2 + var4] = var5;
            }

        }
    }

    public static class FileEncrypt extends FileBase {
        static final int BLOCK_SIZE = 4096;
        static final int BLOCK_SIZE_MASK = 4095;
        static final int HEADER_LENGTH = 4096;
        private static final byte[] HEADER = "H2encrypt\n".getBytes();
        private static final int SALT_POS;
        private static final int SALT_LENGTH = 8;
        private static final int HASH_ITERATIONS = 10;
        private final FileChannel base;
        private long pos;
        private long size;
        private final String name;
        private XTS xts;
        private byte[] encryptionKey;

        public FileEncrypt(String var1, byte[] var2, FileChannel var3) {
            this.name = var1;
            this.base = var3;
            this.encryptionKey = var2;
        }

        private void init() throws IOException {
            if (this.xts == null) {
                this.size = this.base.size() - 4096L;
                boolean var1 = this.size < 0L;
                byte[] var2;
                if (var1) {
                    byte[] var3 = Arrays.copyOf(HEADER, 4096);
                    var2 = MathUtils.secureRandomBytes(8);
                    System.arraycopy(var2, 0, var3, SALT_POS, var2.length);
                    writeFully(this.base, 0L, ByteBuffer.wrap(var3));
                    this.size = 0L;
                } else {
                    var2 = new byte[8];
                    readFully(this.base, (long)SALT_POS, ByteBuffer.wrap(var2));
                    if ((this.size & 4095L) != 0L) {
                        this.size -= 4096L;
                    }
                }

                AES var4 = new AES();
                var4.setKey(SHA256.getPBKDF2(this.encryptionKey, var2, 10, 16));
                this.encryptionKey = null;
                this.xts = new XTS(var4);
            }
        }

        protected void implCloseChannel() throws IOException {
            this.base.close();
        }

        public FileChannel position(long var1) throws IOException {
            this.pos = var1;
            return this;
        }

        public long position() throws IOException {
            return this.pos;
        }

        public int read(ByteBuffer var1) throws IOException {
            int var2 = this.read(var1, this.pos);
            if (var2 > 0) {
                this.pos += (long)var2;
            }

            return var2;
        }

        public int read(ByteBuffer var1, long var2) throws IOException {
            int var4 = var1.remaining();
            if (var4 == 0) {
                return 0;
            } else {
                this.init();
                var4 = (int)Math.min((long)var4, this.size - var2);
                if (var2 >= this.size) {
                    return -1;
                } else if (var2 < 0L) {
                    throw new IllegalArgumentException("pos: " + var2);
                } else if ((var2 & 4095L) == 0L && (var4 & 4095) == 0) {
                    this.readInternal(var1, var2, var4);
                    return var4;
                } else {
                    long var5 = var2 / 4096L * 4096L;
                    int var7 = (int)(var2 - var5);
                    int var8 = (var4 + var7 + 4096 - 1) / 4096 * 4096;
                    ByteBuffer var9 = ByteBuffer.allocate(var8);
                    this.readInternal(var9, var5, var8);
                    var9.flip();
                    var9.limit(var7 + var4);
                    var9.position(var7);
                    var1.put(var9);
                    return var4;
                }
            }
        }

        private void readInternal(ByteBuffer var1, long var2, int var4) throws IOException {
            int var5 = var1.position();
            readFully(this.base, var2 + 4096L, var1);

            for(long var6 = var2 / 4096L; var4 > 0; var4 -= 4096) {
                this.xts.decrypt(var6++, 4096, var1.array(), var1.arrayOffset() + var5);
                var5 += 4096;
            }

        }

        private static void readFully(FileChannel var0, long var1, ByteBuffer var3) throws IOException {
            do {
                int var4 = var0.read(var3, var1);
                if (var4 < 0) {
                    throw new EOFException();
                }

                var1 += (long)var4;
            } while(var3.remaining() > 0);

        }

        public int write(ByteBuffer var1, long var2) throws IOException {
            this.init();
            int var4 = var1.remaining();
            long var5;
            if ((var2 & 4095L) == 0L && (var4 & 4095) == 0) {
                this.writeInternal(var1, var2, var4);
                var5 = var2 + (long)var4;
                this.size = Math.max(this.size, var5);
                return var4;
            } else {
                var5 = var2 / 4096L * 4096L;
                int var7 = (int)(var2 - var5);
                int var8 = (var4 + var7 + 4096 - 1) / 4096 * 4096;
                ByteBuffer var9 = ByteBuffer.allocate(var8);
                int var10 = (int)(this.size - var5 + 4096L - 1L) / 4096 * 4096;
                int var11 = Math.min(var8, var10);
                if (var11 > 0) {
                    this.readInternal(var9, var5, var11);
                    var9.rewind();
                }

                var9.limit(var7 + var4);
                var9.position(var7);
                var9.put(var1);
                var9.limit(var8);
                var9.rewind();
                this.writeInternal(var9, var5, var8);
                long var12 = var2 + (long)var4;
                this.size = Math.max(this.size, var12);
                int var14 = (int)(this.size & 4095L);
                if (var14 > 0) {
                    var9 = ByteBuffer.allocate(var14);
                    writeFully(this.base, var5 + 4096L + (long)var8, var9);
                }

                return var4;
            }
        }

        private void writeInternal(ByteBuffer var1, long var2, int var4) throws IOException {
            ByteBuffer var5 = ByteBuffer.allocate(var4);
            var5.put(var1);
            var5.flip();
            long var6 = var2 / 4096L;
            int var8 = 0;

            for(int var9 = var4; var9 > 0; var9 -= 4096) {
                this.xts.encrypt(var6++, 4096, var5.array(), var5.arrayOffset() + var8);
                var8 += 4096;
            }

            writeFully(this.base, var2 + 4096L, var5);
        }

        private static void writeFully(FileChannel var0, long var1, ByteBuffer var3) throws IOException {
            int var4 = 0;

            do {
                int var5 = var0.write(var3, var1 + (long)var4);
                var4 += var5;
            } while(var3.remaining() > 0);

        }

        public int write(ByteBuffer var1) throws IOException {
            int var2 = this.write(var1, this.pos);
            if (var2 > 0) {
                this.pos += (long)var2;
            }

            return var2;
        }

        public long size() throws IOException {
            this.init();
            return this.size;
        }

        public FileChannel truncate(long var1) throws IOException {
            this.init();
            if (var1 > this.size) {
                return this;
            } else if (var1 < 0L) {
                throw new IllegalArgumentException("newSize: " + var1);
            } else {
                int var3 = (int)(var1 & 4095L);
                if (var3 > 0) {
                    this.base.truncate(var1 + 4096L + 4096L);
                } else {
                    this.base.truncate(var1 + 4096L);
                }

                this.size = var1;
                this.pos = Math.min(this.pos, this.size);
                return this;
            }
        }

        public void force(boolean var1) throws IOException {
            this.base.force(var1);
        }

        public FileLock tryLock(long var1, long var3, boolean var5) throws IOException {
            return this.base.tryLock(var1, var3, var5);
        }

        public String toString() {
            return this.name;
        }

        static {
            SALT_POS = HEADER.length;
        }
    }
}

package org.dizitart.no2.support.crypto;

import org.dizitart.no2.common.util.SecureString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EncryptionProperties {
    protected final String encryptAlgo;
    protected final int tagLengthBit;
    protected final int ivLengthByte;
    protected final int saltLengthByte;
    protected final Charset UTF_8 = StandardCharsets.UTF_8;
    protected final SecureString password;

    public EncryptionProperties(String password){
        this(password, "AES/GCM/NoPadding", 128, 12, 16);
    }

    public EncryptionProperties(String password, String encryptAlgo, int tagLengthBit, int ivLengthByte, int saltLengthByte) {
        this.password = new SecureString(password);
        this.encryptAlgo = encryptAlgo;
        this.tagLengthBit = tagLengthBit;
        this.ivLengthByte = ivLengthByte;
        this.saltLengthByte = saltLengthByte;
    }
}

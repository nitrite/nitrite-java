/*
 * Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.dizitart.no2.mvstore.compat.v1.mvstore.fs;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class FilePathNio extends FilePathWrapper {
    public FilePathNio() {
    }

    public FileChannel open(String var1) throws IOException {
        return new FileNio(this.name.substring(this.getScheme().length() + 1), var1);
    }

    public String getScheme() {
        return "nio";
    }
}

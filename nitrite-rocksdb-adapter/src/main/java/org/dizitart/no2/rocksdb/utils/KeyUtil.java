/*
 * Copyright (c) 2019-2020. Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dizitart.no2.rocksdb.utils;

import org.dizitart.no2.exceptions.NitriteException;

import java.util.List;

import static org.dizitart.no2.rocksdb.Constants.DB_NULL;
import static org.dizitart.no2.rocksdb.Constants.PREFIX_DELIMITER;
import static org.dizitart.no2.rocksdb.utils.ByteUtil.concat;

/**
 * @author Anindya Chatterjee
 */
public class KeyUtil {
    private KeyUtil() {}

    public static byte[] prefixedKey(byte[] prefix, byte[] rawKey) {
        return concat(prefix, PREFIX_DELIMITER, rawKey);
    }

    public static byte[] rawKey(byte[] prefixedKey) {
        List<byte[]> splits = ByteUtil.split(prefixedKey, PREFIX_DELIMITER);
        if (splits.size() != 1 && splits.size() != 2) {
            throw new NitriteException("invalid prefixed key found");
        }

        if (splits.size() == 2) {
            return splits.get(1);
        } else {
            return DB_NULL;
        }
    }

    public static byte[] prefix(byte[] prefixedKey) {
        List<byte[]> splits = ByteUtil.split(prefixedKey, PREFIX_DELIMITER);
        if (splits.size() != 1 && splits.size() != 2) {
            throw new NitriteException("invalid prefixed key found");
        }

        return splits.get(0);
    }
}

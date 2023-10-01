/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.collection;


import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generate unique IDs using the Twitter Snowflake algorithm (see <a href="https://github.com/twitter/snowflake">Snowflake</a>).
 * Snowflake IDs are 64 bit positive longs composed of:
 *
 * <ul>
 *     <li>41 bits time stamp</li>
 *     <li>10 bits machine id</li>
 *     <li>12 bits sequence number</li>
 *     <li>1 unused sign bit, always set to 0</li>
 * </ul>
 * <p>
 * This is a derivative work of -
 * <a href="https://github.com/apache/marmotta/blob/master/libraries/kiwi/kiwi-triplestore/src/main/java/org/apache/marmotta/kiwi/generator/SnowflakeIDGenerator.java">Sebastian Schaffert</a>
 * </p>
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @since 4.0
 */
@Slf4j(topic = "nitrite")
public class SnowflakeIdGenerator {
    private final SecureRandom random;
    private final long nodeIdBits = 10L;

    private long nodeId;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;
    private static final long no2epoch = 1288834974657L;

    public SnowflakeIdGenerator() {
        random = new SecureRandom();
        long maxNodeId = ~(-1L << nodeIdBits);
        this.nodeId = getNodeId();

        if (this.nodeId > maxNodeId) {
            log.warn("nodeId > maxNodeId; using random node id");
            this.nodeId = random.nextInt((int) maxNodeId) + 1;
        }
        log.debug("initialised with node id {}", this.nodeId);
    }

    protected long tillNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    protected long getNodeId() {
        byte[] uuid = asBytes(UUID.randomUUID());
        byte rndByte = (byte) (random.nextInt() & 0x000000FF);

        return ((0x000000FF & (long) uuid[uuid.length - 1]) | (0x0000FF00 & (((long) rndByte) << 8))) >> 6;
    }

    /**
     * Return the next unique id for the type with the given name using the generator's id generation strategy.
     *
     * @return next unique id
     */
    public synchronized long getId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            log.warn("Clock moved backwards. Refusing to generate id for {} milliseconds.", (lastTimestamp - timestamp));
            try {
                Thread.sleep((lastTimestamp - timestamp));
            } catch (InterruptedException ignore) {
            }
        }
        long sequenceBits = 12L;
        if (lastTimestamp == timestamp) {
            long sequenceMask = ~(-1L << sequenceBits);
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tillNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        long timestampLeftShift = sequenceBits + nodeIdBits;
        long id = ((timestamp - no2epoch) << timestampLeftShift) | (nodeId << sequenceBits) | sequence;

        if (id < 0) {
            log.warn("Generated id is negative: {}", id);
        }
        return id;
    }

    private byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
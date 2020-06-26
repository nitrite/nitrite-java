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

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Generate unique IDs using the Twitter Snowflake algorithm (see https://github.com/twitter/snowflake). Snowflake IDs
 * are 64 bit positive longs composed of:
 *
 * <ul>
 *     <li>41 bits time stamp</li>
 *     <li>10 bits machine id</li>
 *     <li>12 bits sequence number</li>
 *     <li>1 unused sign bit, always set to 0</li>
 * </ul>
 * <p>
 * Original code can be found here - https://github.com/apache/marmotta/blob/master/libraries/kiwi/kiwi-triplestore/src/main/java/org/apache/marmotta/kiwi/generator/SnowflakeIDGenerator.java
 * </p>
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 * @since 4.0
 */
@Slf4j
public class SnowflakeIdGenerator {
    private final long nodeIdBits = 10L;
    private final long maxNodeId = ~(-1L << nodeIdBits);
    private final long sequenceBits = 12L;

    private final long nodeIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + nodeIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);

    private final long twepoch = 1288834974657L;
    private long nodeId;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;


    public SnowflakeIdGenerator() {
        try {
            this.nodeId = getNodeId();
        } catch (SocketException | NoSuchElementException | NullPointerException e) {
            log.warn("SNOWFLAKE: could not determine machine address; using random node id");
            Random rnd = new Random();
            this.nodeId = rnd.nextInt((int) maxNodeId) + 1;
        }

        if (this.nodeId > maxNodeId) {
            log.warn("SNOWFLAKE: nodeId > maxNodeId; using random node id");
            Random rnd = new Random();
            this.nodeId = rnd.nextInt((int) maxNodeId) + 1;
        }
        log.debug("SNOWFLAKE: initialised with node id {}", this.nodeId);
    }

    protected long tillNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    protected long getNodeId() throws SocketException {
        NetworkInterface network = null;

        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface nint = en.nextElement();
            if (!nint.isLoopback() && nint.getHardwareAddress() != null) {
                network = nint;
                break;
            }
        }

        if (network != null) {
            byte[] mac = network.getHardwareAddress();
            Random rnd = new Random();
            byte rndByte = (byte) (rnd.nextInt() & 0x000000FF);

            // take the last byte of the MAC address and a random byte as node id
            return ((0x000000FF & (long) mac[mac.length - 1]) | (0x0000FF00 & (((long) rndByte) << 8))) >> 6;
        } else {
            throw new NoSuchElementException("no network interface found");
        }
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
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tillNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (nodeId << nodeIdShift) | sequence;

        if (id < 0) {
            log.warn("Id is smaller than 0: {}", id);
        }
        return id;
    }
}
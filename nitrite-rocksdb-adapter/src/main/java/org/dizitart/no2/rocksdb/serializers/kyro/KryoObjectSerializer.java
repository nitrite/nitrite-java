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

package org.dizitart.no2.rocksdb.serializers.kyro;


import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.rocksdb.serializers.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.dizitart.no2.rocksdb.Constants.DB_NULL;

/**
 * @since 4.0
 * @author Anindya Chatterjee
 */
@Slf4j(topic = "nitrite-rocksdb")
public class KryoObjectSerializer implements ObjectSerializer {
    private static final Kryo kryo = new Kryo();
    private final Map<Class<?>, KryoKeySerializer<?>> keySerializerRegistry;

    public KryoObjectSerializer() {
        this.keySerializerRegistry = new HashMap<>();
        kryo.setRegistrationRequired(false);
        registerInternalSerializers();
    }

    @Override
    public <T> byte[] encode(T object) {
        if (object == null) return DB_NULL;

        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (Output output = new Output(byteArrayOutputStream)) {
                synchronized (kryo) {
                    kryo.writeObject(output, object);
                }
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new NitriteIOException("Failed to close output stream", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> byte[] encodeKey(T object) {
        if (object == null) return DB_NULL;

        Class<?> clazz = object.getClass();
        KryoKeySerializer<T> serializer = (KryoKeySerializer<T>) keySerializerRegistry.get(clazz);
        if (serializer == null) {
            return encode(object);
        }

        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (Output output = new Output(byteArrayOutputStream)) {
                serializer.writeKey(kryo, output, object);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new NitriteIOException("Failed to close output stream", e);
        }
    }

    @Override
    public <T> T decode(byte[] bytes, Class<T> type) {
        if (Arrays.equals(bytes, DB_NULL)) return null;

        try (Input input = new Input(bytes, 0, bytes.length)) {
            synchronized (kryo) {
                return kryo.readObject(input, type);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decodeKey(byte[] bytes, Class<T> type) {
        if (Arrays.equals(bytes, DB_NULL)) return null;

        KryoKeySerializer<T> serializer = (KryoKeySerializer<T>) keySerializerRegistry.get(type);
        if (serializer == null) {
            return decode(bytes, type);
        }

        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            try (Input input = new Input(byteArrayInputStream)) {
                return serializer.readKey(kryo, input, type);
            }
        } catch (IOException e) {
            throw new NitriteIOException("Failed to close output stream", e);
        }
    }

    public void registerSerializer(Class<?> type, Serializer<?> serializer) {
        if (serializer instanceof KryoKeySerializer) {
            KryoKeySerializer<?> kryoKeySerializer = (KryoKeySerializer<?>) serializer;
            if (kryoKeySerializer.registerToKryo()) {
                kryo.register(type, serializer);
            }
            keySerializerRegistry.put(type, kryoKeySerializer);
        } else {
            kryo.register(type, serializer);
        }
    }

    private void registerInternalSerializers() {
        try {
            NitriteSerializers.registerAll(this);
            DefaultJavaSerializers.registerAll(this);
            DefaultTimeKeySerializers.registerAll(this);
        } catch (Exception e) {
            log.error("Error while registering default serializers", e);
            throw new NitriteIOException("Failed to register default serializers", e);
        }
    }
}

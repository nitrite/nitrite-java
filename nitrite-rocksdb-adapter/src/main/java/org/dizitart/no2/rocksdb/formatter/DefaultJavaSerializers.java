package org.dizitart.no2.rocksdb.formatter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
public class DefaultJavaSerializers {

    private static class ArraysAsListSerializer extends Serializer<List> {
        private final Field _arrayField;

        public ArraysAsListSerializer() {
            try {
                _arrayField = Class.forName( "java.util.Arrays$ArrayList" ).getDeclaredField( "a" );
                _arrayField.setAccessible( true );
            } catch ( final Exception e ) {
                throw new RuntimeException( e );
            }
        }

        @Override
        public List<?> read(Kryo kryo, final Input input, final Class<List> type) {
            final int length = input.readInt(true);
            Class<?> componentType = kryo.readClass( input ).getType();
            if (componentType.isPrimitive()) {
                componentType = getPrimitiveWrapperClass(componentType);
            }
            try {
                final Object items = Array.newInstance( componentType, length );
                for( int i = 0; i < length; i++ ) {
                    Array.set(items, i, kryo.readClassAndObject( input ));
                }
                return Arrays.asList( (Object[])items );
            } catch ( final Exception e ) {
                throw new RuntimeException( e );
            }
        }

        @Override
        public void write(final Kryo kryo, final Output output, final List obj) {
            try {
                final Object[] array = (Object[]) _arrayField.get( obj );
                output.writeInt(array.length, true);
                final Class<?> componentType = array.getClass().getComponentType();
                kryo.writeClass( output, componentType );
                for( final Object item : array ) {
                    kryo.writeClassAndObject( output, item );
                }
            } catch ( final RuntimeException e ) {
                // Don't eat and wrap RuntimeExceptions because the ObjectBuffer.write...
                // handles SerializationException specifically (resizing the buffer)...
                throw e;
            } catch ( final Exception e ) {
                throw new RuntimeException( e );
            }
        }

        @Override
        public List<?> copy(Kryo kryo, List original) {
            try {
                final Object[] array = (Object[]) _arrayField.get(original);
                kryo.reference(array);
                Object[] arrayCopy = kryo.copy(array);
                return Arrays.asList(arrayCopy);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static Class<?> getPrimitiveWrapperClass(final Class<?> c) {
            if (c.isPrimitive()) {
                if (c.equals(Long.TYPE)) {
                    return Long.class;
                } else if (c.equals(Integer.TYPE)) {
                    return Integer.class;
                } else if (c.equals(Double.TYPE)) {
                    return Double.class;
                } else if (c.equals(Float.TYPE)) {
                    return Float.class;
                } else if (c.equals(Boolean.TYPE)) {
                    return Boolean.class;
                } else if (c.equals(Character.TYPE)) {
                    return Character.class;
                } else if (c.equals(Short.TYPE)) {
                    return Short.class;
                } else if (c.equals(Byte.TYPE)) {
                    return Byte.class;
                }
            }
            return c;
        }
    }

    private static class UUIDSerializer extends Serializer<UUID> {

        @Override
        public void write(Kryo kryo, Output output, UUID object) {
            output.writeString(object.toString());
        }

        @Override
        public UUID read(Kryo kryo, Input input, Class<UUID> type) {
            return UUID.fromString(input.readString());
        }
    }

    public static void registerAll(KryoObjectFormatter kryoObjectFormatter) {
        kryoObjectFormatter.registerSerializer(Arrays.asList("a", "b").getClass(), new ArraysAsListSerializer());
        kryoObjectFormatter.registerSerializer(UUID.class, new UUIDSerializer());
    }
}

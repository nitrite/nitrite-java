package org.dizitart.no2.rocksdb.serializers.kyro;


import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.joda.time.DateTime;

/**
 * @author Anindya Chatterjee
 */
public class JodaTimeKryoKeySerializer extends ComparableKeySerializer<DateTime> {

    @Override
    public void write(Kryo kryo, Output output, DateTime object) {
        output.writeLong(object.getMillis());
    }

    @Override
    public DateTime read(Kryo kryo, Input input, Class<? extends DateTime> type) {
        return new DateTime(input.readLong());
    }

    @Override
    protected void writeKeyInternal(Kryo kryo, Output output, DateTime object) {
        output.writeString(object.toString());
    }

    @Override
    protected DateTime readKeyInternal(Kryo kryo, String input, Class<DateTime> type) {
        return DateTime.parse(input);
    }

    @Override
    public boolean registerToKryo() {
        return true;
    }
}

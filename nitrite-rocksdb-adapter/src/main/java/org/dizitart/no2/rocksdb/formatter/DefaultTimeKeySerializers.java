package org.dizitart.no2.rocksdb.formatter;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.dizitart.no2.exceptions.NitriteIOException;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * @author Anindya Chatterjee
 */
class DefaultTimeKeySerializers {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);

    private static class DateSerializer extends ComparableKeySerializer<Date> {

        @Override
        public void writeKeyInternal(Kryo kryo, Output output, Date object) {
            output.writeString(format.format(object));
        }

        @Override
        public Date readKeyInternal(Kryo kryo, String value, Class<Date> type) {
            try {
                return format.parse(value);
            } catch (Exception e) {
                throw new NitriteIOException("failed to read java.util.Date", e);
            }
        }
    }

    private static class TimestampSerializer extends ComparableKeySerializer<Timestamp> {

        @Override
        public void writeKeyInternal(Kryo kryo, Output output, Timestamp object) {
            output.writeString(format.format(object));
        }

        @Override
        public Timestamp readKeyInternal(Kryo kryo, String value, Class<Timestamp> type) {
            try {
                return new Timestamp(format.parse(value).getTime());
            } catch (Exception e) {
                throw new NitriteIOException("failed to read java.sql.Timestamp", e);
            }
        }
    }

    private static class SqlDateSerializer extends ComparableKeySerializer<java.sql.Date> {

        @Override
        public void writeKeyInternal(Kryo kryo, Output output, java.sql.Date object) {
            output.writeString(format.format(object));
        }

        @Override
        public java.sql.Date readKeyInternal(Kryo kryo, String value, Class<java.sql.Date> type) {
            try {
                return new java.sql.Date(format.parse(value).getTime());
            } catch (Exception e) {
                throw new NitriteIOException("failed to read java.sql.Date", e);
            }
        }
    }

    private static class TimeSerializer extends ComparableKeySerializer<Time> {

        @Override
        public void writeKeyInternal(Kryo kryo, Output output, Time object) {
            output.writeString(format.format(object));
        }

        @Override
        public Time readKeyInternal(Kryo kryo, String value, Class<Time> type) {
            try {
                return new Time(format.parse(value).getTime());
            } catch (Exception e) {
                throw new NitriteIOException("failed to read java.sql.Time", e);
            }
        }
    }

    private static class CalendarSerializer extends ComparableKeySerializer<Calendar> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, Calendar object) {
            output.writeString(format.format(object.getTime()));
        }

        @Override
        protected Calendar readKeyInternal(Kryo kryo, String input, Class<Calendar> type) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(format.parse(input));
                return cal;
            } catch (Exception e) {
                throw new NitriteIOException("failed to read java.util.Date", e);
            }
        }
    }

    private static class GregorianCalendarSerializer extends CalendarSerializer {
    }

    public static void registerAll(KryoObjectFormatter kryoObjectFormatter) {
        kryoObjectFormatter.registerSerializer(Date.class, new DateSerializer());
        kryoObjectFormatter.registerSerializer(Timestamp.class, new TimestampSerializer());
        kryoObjectFormatter.registerSerializer(java.sql.Date.class, new SqlDateSerializer());
        kryoObjectFormatter.registerSerializer(Time.class, new TimeSerializer());
        kryoObjectFormatter.registerSerializer(GregorianCalendar.class, new GregorianCalendarSerializer());
    }
}

package org.dizitart.no2.rocksdb.formatter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.dizitart.no2.exceptions.NitriteIOException;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
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

    private static class DurationSerializer extends ComparableKeySerializer<Duration> {

        @Override
        public void writeKeyInternal(Kryo kryo, Output output, Duration object) {
            output.writeString(object.toString());
        }

        @Override
        public Duration readKeyInternal(Kryo kryo, String value, Class<Duration> type) {
            return Duration.parse(value);
        }
    }

    private static class InstantSerializer extends ComparableKeySerializer<Instant> {

        @Override
        public void writeKeyInternal(Kryo kryo, Output output, Instant object) {
            output.writeString(object.toString());
        }

        @Override
        public Instant readKeyInternal(Kryo kryo, String value, Class<Instant> type) {
            return Instant.parse(value);
        }
    }

    private static class LocalDateSerializer extends ComparableKeySerializer<LocalDate> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, LocalDate object) {
            output.writeString(object.toString());
        }

        @Override
        protected LocalDate readKeyInternal(Kryo kryo, String input, Class<LocalDate> type) {
            return LocalDate.parse(input);
        }
    }

    private static class LocalDateTimeSerializer extends ComparableKeySerializer<LocalDateTime> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, LocalDateTime object) {
            output.writeString(object.toString());
        }

        @Override
        protected LocalDateTime readKeyInternal(Kryo kryo, String input, Class<LocalDateTime> type) {
            return LocalDateTime.parse(input);
        }
    }

    private static class LocalTimeSerializer extends ComparableKeySerializer<LocalTime> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, LocalTime object) {
            output.writeString(object.toString());
        }

        @Override
        protected LocalTime readKeyInternal(Kryo kryo, String input, Class<LocalTime> type) {
            return LocalTime.parse(input);
        }
    }

    private static class ZoneOffsetSerializer extends ComparableKeySerializer<ZoneOffset> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, ZoneOffset object) {
            output.writeString(object.toString());
        }

        @Override
        protected ZoneOffset readKeyInternal(Kryo kryo, String input, Class<ZoneOffset> type) {
            return ZoneOffset.of(input);
        }
    }

    private static class OffsetTimeSerializer extends ComparableKeySerializer<OffsetTime> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, OffsetTime object) {
            output.writeString(object.toString());
        }

        @Override
        protected OffsetTime readKeyInternal(Kryo kryo, String input, Class<OffsetTime> type) {
            return OffsetTime.parse(input);
        }
    }

    private static class OffsetDateTimeSerializer extends ComparableKeySerializer<OffsetDateTime> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, OffsetDateTime object) {
            output.writeString(object.toString());
        }

        @Override
        protected OffsetDateTime readKeyInternal(Kryo kryo, String input, Class<OffsetDateTime> type) {
            return OffsetDateTime.parse(input);
        }
    }

    private static class ZonedDateTimeSerializer extends ComparableKeySerializer<ZonedDateTime> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, ZonedDateTime object) {
            output.writeString(object.toString());
        }

        @Override
        protected ZonedDateTime readKeyInternal(Kryo kryo, String input, Class<ZonedDateTime> type) {
            return ZonedDateTime.parse(input);
        }
    }

    private static class YearSerializer extends ComparableKeySerializer<Year> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, Year object) {
            output.writeString(object.toString());
        }

        @Override
        protected Year readKeyInternal(Kryo kryo, String input, Class<Year> type) {
            return Year.parse(input);
        }
    }

    private static class YearMonthSerializer extends ComparableKeySerializer<YearMonth> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, YearMonth object) {
            output.writeString(object.toString());
        }

        @Override
        protected YearMonth readKeyInternal(Kryo kryo, String input, Class<YearMonth> type) {
            return YearMonth.parse(input);
        }
    }

    private static class MonthDaySerializer extends ComparableKeySerializer<MonthDay> {

        @Override
        protected void writeKeyInternal(Kryo kryo, Output output, MonthDay object) {
            output.writeString(object.toString());
        }

        @Override
        protected MonthDay readKeyInternal(Kryo kryo, String input, Class<MonthDay> type) {
            return MonthDay.parse(input);
        }
    }

    public static void registerAll(KryoObjectFormatter kryoObjectFormatter) {
        kryoObjectFormatter.registerSerializer(Date.class, new DateSerializer());
        kryoObjectFormatter.registerSerializer(Timestamp.class, new TimestampSerializer());
        kryoObjectFormatter.registerSerializer(java.sql.Date.class, new SqlDateSerializer());
        kryoObjectFormatter.registerSerializer(Time.class, new TimeSerializer());
        kryoObjectFormatter.registerSerializer(GregorianCalendar.class, new GregorianCalendarSerializer());
        kryoObjectFormatter.registerSerializer(Duration.class, new DurationSerializer());
        kryoObjectFormatter.registerSerializer(Instant.class, new InstantSerializer());
        kryoObjectFormatter.registerSerializer(LocalDate.class, new LocalDateSerializer());
        kryoObjectFormatter.registerSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        kryoObjectFormatter.registerSerializer(LocalTime.class, new LocalTimeSerializer());
        kryoObjectFormatter.registerSerializer(ZoneOffset.class, new ZoneOffsetSerializer());
        kryoObjectFormatter.registerSerializer(OffsetTime.class, new OffsetTimeSerializer());
        kryoObjectFormatter.registerSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
        kryoObjectFormatter.registerSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer());
        kryoObjectFormatter.registerSerializer(Year.class, new YearSerializer());
        kryoObjectFormatter.registerSerializer(YearMonth.class, new YearMonthSerializer());
        kryoObjectFormatter.registerSerializer(MonthDay.class, new MonthDaySerializer());
    }
}

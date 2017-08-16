package test.avro;

import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.io.*;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.apache.avro.Schema.Type.*;
import static org.apache.avro.Schema.createUnion;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AvroTypesTest {

    @Test
    public void basicTypes() throws Throwable {
        assertThat(copy(3, as(INT)), equalTo(3));
        assertThat(copy(true, as(BOOLEAN)), equalTo(true));
        assertThat(copy("foo", as(STRING)), equalTo("foo"));
    }


    @Test
    public void narrowLongToInt() throws Throwable {
        assertThat(copy(3L, as(INT)), equalTo(3));
    }

    @Test
    public void nothing() throws Throwable {
        assertThat(copy(null, as(NULL)), is(nullValue()));
    }

    @Test
    public void union() throws Throwable {
        Schema schema = createUnion(as(INT), as(NULL));

        assertThat(copy(null, schema), is(nullValue()));
        assertThat(copy(1, schema), equalTo(1));
    }

    @Test
    public void localDate() throws Throwable {
        LocalDate date = LocalDate.now();

        assertThat(copy(date, LogicalTypes.date().addToSchema(as(INT))), equalTo(date));
    }


    @Test
    public void record() throws Throwable {
        Record record = new Record(1, LocalDate.now());

        //@formatter:off
        Schema schema = schema("{" +
            "namespace:test.avro, " +
            "name: Record, " +
            "type: record, " +
            "fields:[" +
                "{name:id, type:int}," +
                "{name:createdAt, type:{type:int, logicalType:date}}" +
            "]" +
        "}");
        //@formatter:on

        assertThat(copy(record, schema), equalTo(record));
    }

    private Schema as(Schema.Type type) {
        return Schema.create(type);
    }

    private Schema schema(String schema) {
        return new Schema.Parser().parse(schema.replaceAll("(\\w+(?:[.\\$]\\w+)?)", "\"$1\""));
    }

    private static <T> T copy(T value, Schema schema) throws IOException {
        return deserialize(serialize(value, schema), schema);
    }

    private static <T> byte[] serialize(T value, Schema schema) throws IOException {
        DatumWriter<T> out = new ReflectDatumWriter<>(schema, data());
        ByteArrayOutputStream dest = new ByteArrayOutputStream();

        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, dest);
        out.write(value, encoder);
        encoder.flush();

        return dest.toByteArray();
    }

    private static <T> T deserialize(byte[] source, Schema schema) throws IOException {
        DatumReader<T> in = new ReflectDatumReader<>(schema, schema, data());
        return in.read(null, DecoderFactory.get().jsonDecoder(schema, new ByteArrayInputStream(source)));
    }

    private static ReflectData data() {
        ReflectData data = new ReflectData();
        data.addLogicalTypeConversion(new Java8LocalDateConversion());
        return data;
    }

}

class Record {
    private int id;
    private LocalDate createdAt;

    private Record() {
    }

    public Record(int id, LocalDate createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public boolean equals(Object o) {
        return equals((Record) o);
    }

    public boolean equals(Record that) {
        return id == that.id && createdAt.equals(that.createdAt);
    }
}


class Java8LocalDateConversion extends Conversion<LocalDate> {
    @Override
    public Class<LocalDate> getConvertedType() {
        return LocalDate.class;
    }

    @Override
    public String getLogicalTypeName() {
        return LogicalTypes.date().getName();
    }

    @Override
    public Integer toInt(LocalDate value, Schema schema, LogicalType type) {
        return (int) value.toEpochDay();
    }

    @Override
    public LocalDate fromInt(Integer value, Schema schema, LogicalType type) {
        return LocalDate.ofEpochDay(value);
    }
}

package test.avro;

import org.apache.avro.Conversion;
import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.util.Utf8;
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
    }

    @Test
    public void strings() throws Throwable {
        Utf8 foo = (Utf8) copy((CharSequence) "foo", as(STRING));

        assertThat(foo.toString(), equalTo("foo"));
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


    private Schema as(Schema.Type type) {
        return Schema.create(type);
    }

    private <T> T copy(T value, Schema schema) throws IOException {
        return deserialize(serialize(value, schema), schema);
    }

    private <T> byte[] serialize(T value, Schema schema) throws IOException {
        DatumWriter<T> out = new SpecificDatumWriter<>(schema, data());
        ByteArrayOutputStream dest = new ByteArrayOutputStream();

        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, dest);
        out.write(value, encoder);
        encoder.flush();

        return dest.toByteArray();
    }

    private <T> T deserialize(byte[] source, Schema schema) throws IOException {
        DatumReader<T> in = new SpecificDatumReader<>(schema, schema, data());
        return in.read(null, DecoderFactory.get().jsonDecoder(schema, new ByteArrayInputStream(source)));
    }

    private SpecificData data() {
        SpecificData it = new SpecificData();
        it.addLogicalTypeConversion(new Java8LocalDateConversion());
        return it;
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

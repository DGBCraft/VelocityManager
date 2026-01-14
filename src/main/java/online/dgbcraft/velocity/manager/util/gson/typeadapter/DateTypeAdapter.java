package online.dgbcraft.velocity.manager.util.gson.typeadapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Sanluli36li
 */
public class DateTypeAdapter extends TypeAdapter<Date> {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    public static String fromDate(Date value) {
        if (value == null) {
            return null;
        } else {
            return DATE_FORMAT.format(value);
        }
    }

    public static Date fromString(String input) {
        try {
            return DATE_FORMAT.parse(input);
        } catch (ParseException e) {
            return new Date();
        }
    }

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        out.value(fromDate(value));
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        return fromString(in.nextString());
    }
}

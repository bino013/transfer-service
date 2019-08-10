package ph.caleon.transfer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public final class JSONUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.findAndRegisterModules();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    private JSONUtil(){}

    public static String toString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse object.", e);
        }
    }

    public static <T> T toObject(String value, Class<T> clazz) {
        try {
            return mapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert to object " + clazz.getName(), e);
        }
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}

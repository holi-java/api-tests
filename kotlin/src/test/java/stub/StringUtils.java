package stub;

public class StringUtils {
    public static String toNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}

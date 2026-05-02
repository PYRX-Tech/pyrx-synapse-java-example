package tech.pyrx.synapse.internal;

import java.util.*;

/**
 * Minimal zero-dependency JSON serializer and deserializer.
 * Handles objects (Map), arrays (List), strings, numbers, booleans, and nulls.
 */
public final class JsonUtil {

    private JsonUtil() {}

    // -----------------------------------------------------------------------
    // Serialization
    // -----------------------------------------------------------------------

    public static String serialize(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, value);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Map) {
            writeObject(sb, (Map<String, Object>) value);
        } else if (value instanceof List) {
            writeArray(sb, (List<Object>) value);
        } else if (value instanceof String) {
            writeString(sb, (String) value);
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Number) {
            Number num = (Number) value;
            // Emit integers without decimal point
            if (value instanceof Integer || value instanceof Long) {
                sb.append(num.longValue());
            } else {
                double d = num.doubleValue();
                if (d == Math.floor(d) && !Double.isInfinite(d) && d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                    sb.append((long) d);
                } else {
                    sb.append(d);
                }
            }
        } else if (value instanceof Object[]) {
            writeArray(sb, Arrays.asList((Object[]) value));
        } else {
            // Fallback: treat as string
            writeString(sb, value.toString());
        }
    }

    private static void writeObject(StringBuilder sb, Map<String, Object> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            writeString(sb, entry.getKey());
            sb.append(':');
            writeValue(sb, entry.getValue());
        }
        sb.append('}');
    }

    private static void writeArray(StringBuilder sb, List<Object> list) {
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            writeValue(sb, list.get(i));
        }
        sb.append(']');
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    // -----------------------------------------------------------------------
    // Deserialization — simple recursive descent parser
    // -----------------------------------------------------------------------

    public static Map<String, Object> deserializeObject(String json) {
        Parser p = new Parser(json.trim());
        Object result = p.parseValue();
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) result;
            return map;
        }
        throw new IllegalArgumentException("Expected JSON object, got: " + (result == null ? "null" : result.getClass().getSimpleName()));
    }

    public static List<Object> deserializeArray(String json) {
        Parser p = new Parser(json.trim());
        Object result = p.parseValue();
        if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) result;
            return list;
        }
        throw new IllegalArgumentException("Expected JSON array, got: " + (result == null ? "null" : result.getClass().getSimpleName()));
    }

    /**
     * Parse a JSON string and return the top-level value.
     * May return Map, List, String, Number, Boolean, or null.
     */
    public static Object deserialize(String json) {
        Parser p = new Parser(json.trim());
        return p.parseValue();
    }

    // -----------------------------------------------------------------------
    // Helper: extract typed values from Map
    // -----------------------------------------------------------------------

    public static String stringFromMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v instanceof String ? (String) v : "";
    }

    public static int intFromMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return 0;
    }

    public static double doubleFromMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        return 0.0;
    }

    public static boolean boolFromMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> mapFromMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Map) {
            return (Map<String, Object>) v;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> listFromMap(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof List) {
            return (List<Object>) v;
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Recursive descent JSON parser
    // -----------------------------------------------------------------------

    private static class Parser {
        private final String input;
        private int pos;

        Parser(String input) {
            this.input = input;
            this.pos = 0;
        }

        Object parseValue() {
            skipWhitespace();
            if (pos >= input.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            char c = input.charAt(pos);
            switch (c) {
                case '{': return parseObject();
                case '[': return parseArray();
                case '"': return parseString();
                case 't': case 'f': return parseBoolean();
                case 'n': return parseNull();
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return parseNumber();
                    }
                    throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
            }
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (pos < input.length() && input.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (pos >= input.length()) {
                    throw new IllegalArgumentException("Unexpected end of JSON in object");
                }
                if (input.charAt(pos) == '}') {
                    pos++;
                    return map;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (pos < input.length() && input.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (true) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (pos >= input.length()) {
                    throw new IllegalArgumentException("Unexpected end of JSON in array");
                }
                if (input.charAt(pos) == ']') {
                    pos++;
                    return list;
                }
                expect(',');
            }
        }

        private String parseString() {
            skipWhitespace();
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (c == '"') {
                    pos++;
                    return sb.toString();
                }
                if (c == '\\') {
                    pos++;
                    if (pos >= input.length()) {
                        throw new IllegalArgumentException("Unexpected end of string escape");
                    }
                    char esc = input.charAt(pos);
                    switch (esc) {
                        case '"':  sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/':  sb.append('/'); break;
                        case 'b':  sb.append('\b'); break;
                        case 'f':  sb.append('\f'); break;
                        case 'n':  sb.append('\n'); break;
                        case 'r':  sb.append('\r'); break;
                        case 't':  sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 >= input.length()) {
                                throw new IllegalArgumentException("Invalid unicode escape");
                            }
                            String hex = input.substring(pos + 1, pos + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                            break;
                        default:
                            sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
                pos++;
            }
            throw new IllegalArgumentException("Unterminated string");
        }

        private Number parseNumber() {
            int start = pos;
            if (pos < input.length() && input.charAt(pos) == '-') pos++;
            while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
            boolean isFloat = false;
            if (pos < input.length() && input.charAt(pos) == '.') {
                isFloat = true;
                pos++;
                while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
            }
            if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
                isFloat = true;
                pos++;
                if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) pos++;
                while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
            }
            String numStr = input.substring(start, pos);
            if (isFloat) {
                return Double.parseDouble(numStr);
            }
            long val = Long.parseLong(numStr);
            if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                return (int) val;
            }
            return val;
        }

        private Boolean parseBoolean() {
            if (input.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (input.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid boolean at position " + pos);
        }

        private Object parseNull() {
            if (input.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null at position " + pos);
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        private void expect(char expected) {
            if (pos >= input.length() || input.charAt(pos) != expected) {
                throw new IllegalArgumentException(
                    "Expected '" + expected + "' at position " + pos +
                    (pos < input.length() ? ", got '" + input.charAt(pos) + "'" : ", got end of input"));
            }
            pos++;
        }
    }
}

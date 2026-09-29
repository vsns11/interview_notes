package ch02.strings;

/** Text blocks (Java 15): multi-line string literals. */
public class TextBlocks {
    public static void main(String[] args) {
        String json = """
                {
                  "name": "Ada",
                  "langs": ["Java", "SQL"]
                }
                """;
        System.out.print(json);

        String sql = """
            SELECT id, name
            FROM users
            WHERE active = true\
            """;                                     // \ at line end: no newline there
        System.out.println("[" + sql + "]");

        String spaces = """
            trailing kept:\s
            quote: \""" inside
            """;
        System.out.print(spaces.replace(' ', '.'));

        String html = """
            <p>Hello, %s!</p>
            """.formatted("World");
        System.out.print(html);
        System.out.println("lines in json: " + json.lines().count());
    }
}

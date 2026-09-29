package lld.ch08.builder;

public class Main {
    public static void main(String[] args) {
        System.out.println("== The problem: telescoping constructors ==");
        // Which boolean is which? Swap two of them and it still compiles.
        System.out.println("  " + new TelescopingPizza("large", "thin", true, false, true));

        System.out.println("\n== Fluent builder: every value is named ==");
        HttpRequest get = HttpRequest.builder("https://api.example.com/orders")
                .header("Accept", "application/json")
                .build();
        System.out.println("  " + get);

        HttpRequest post = HttpRequest.builder("https://api.example.com/orders")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{\"item\":\"widget\"}")
                .timeoutSeconds(5)
                .retries(0)
                .followRedirects(false)
                .build();
        System.out.println("  " + post);

        System.out.println("\n== build() refuses broken objects ==");
        try {
            HttpRequest.builder("https://api.example.com/orders").body("{}").build();
        } catch (IllegalStateException e) {
            System.out.println("  rejected: " + e.getMessage());
        }

        System.out.println("\n== Reusing a builder does not leak state into earlier products ==");
        HttpRequest.Builder reused = HttpRequest.builder("https://api.example.com/users").header("X-Trace", "1");
        HttpRequest first = reused.build();
        HttpRequest second = reused.header("X-Debug", "true").build();
        System.out.println("  first headers  = " + first.headers());
        System.out.println("  second headers = " + new java.util.TreeMap<>(second.headers()));

        System.out.println("\n== Product is immutable ==");
        try {
            post.headers().put("Hack", "yes");
        } catch (UnsupportedOperationException e) {
            System.out.println("  headers().put -> UnsupportedOperationException");
        }

        System.out.println("\n== toBuilder(): copy-and-change, like Lombok @Builder(toBuilder = true) ==");
        HttpRequest slowPost = post.toBuilder().timeoutSeconds(60).build();
        System.out.println("  " + slowPost);

        System.out.println("\n== Director: named recipes over any builder ==");
        RequestDirector director = new RequestDirector();
        System.out.println("  " + director.jsonPost(HttpRequest.builder("https://api.example.com/payments"), "{\"amount\":499}"));
        System.out.println("  " + director.healthCheck(HttpRequest.builder("https://api.example.com/health")));

        System.out.println("\n== Staged builder: the compiler enforces required steps ==");
        Email email = Email.builder()
                .to("ada@example.com")          // must come first
                .subject("Welcome")             // then this
                .body("Glad to have you")       // optional, any order
                .cc("team@example.com")
                .build();
        System.out.println("  " + email);
        // Email.builder().build();   // does not compile: ToStep has no build()
    }
}

package lld.ch10.chain;

/** Same shape as jakarta.servlet.Filter: "next" is an explicit argument, so forgetting it is visible. */
@FunctionalInterface
interface Filter {
    void doFilter(HttpRequest request, StringBuilder response, FilterChain chain);
}

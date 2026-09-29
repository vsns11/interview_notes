package lld.ch10.chain;

/** The endpoint at the end of the chain (like a Servlet). */
@FunctionalInterface
interface Endpoint {
    void service(HttpRequest request, StringBuilder response);
}

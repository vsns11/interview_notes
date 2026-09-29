package lld.ch08.builder;

/** Director: a named, reusable RECIPE (order of steps). It does not know the values the caller varies. */
public final class RequestDirector {

    public HttpRequest jsonPost(HttpRequest.Builder builder, String payload) {
        return builder.method("POST")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(payload)
                .timeoutSeconds(5)
                .build();
    }

    public HttpRequest healthCheck(HttpRequest.Builder builder) {
        return builder.method("HEAD").timeoutSeconds(1).retries(0).followRedirects(false).build();
    }
}

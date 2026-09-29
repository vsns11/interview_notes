package lld.ch08.builder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Product: immutable. Only the nested Builder can create one. */
public final class HttpRequest {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final String body;
    private final int timeoutSeconds;
    private final int retries;
    private final boolean followRedirects;

    private HttpRequest(Builder b) {
        this.url = b.url;
        this.method = b.method;
        this.headers = Map.copyOf(b.headers);      // defensive, unmodifiable copy
        this.body = b.body;
        this.timeoutSeconds = b.timeoutSeconds;
        this.retries = b.retries;
        this.followRedirects = b.followRedirects;
    }

    public static Builder builder(String url) { return new Builder(url); }

    /** Lombok-style toBuilder(): start a new builder pre-filled from this object. */
    public Builder toBuilder() {
        Builder b = new Builder(url).method(method).timeoutSeconds(timeoutSeconds)
                .retries(retries).followRedirects(followRedirects);
        headers.forEach(b::header);
        if (body != null) b.body(body);
        return b;
    }

    public String url() { return url; }
    public String method() { return method; }
    public Map<String, String> headers() { return headers; }

    @Override public String toString() {
        return method + " " + url + " headers=" + new java.util.TreeMap<>(headers)
                + (body == null ? "" : " body=" + body)
                + " timeout=" + timeoutSeconds + "s retries=" + retries + " followRedirects=" + followRedirects;
    }

    /** The builder: same fields, mutable, with defaults. Every setter returns this. */
    public static final class Builder {
        private final String url;                                  // required -> constructor
        private String method = "GET";                             // optional -> defaults
        private final Map<String, String> headers = new LinkedHashMap<>();
        private String body;
        private int timeoutSeconds = 30;
        private int retries = 3;
        private boolean followRedirects = true;

        private Builder(String url) { this.url = Objects.requireNonNull(url, "url"); }

        public Builder method(String m)             { this.method = m; return this; }
        public Builder header(String n, String v)   { headers.put(n, v); return this; }
        public Builder body(String b)               { this.body = b; return this; }
        public Builder timeoutSeconds(int s)        { this.timeoutSeconds = s; return this; }
        public Builder retries(int r)               { this.retries = r; return this; }
        public Builder followRedirects(boolean f)   { this.followRedirects = f; return this; }

        /** The single gate: cross-field validation happens here, before any product exists. */
        public HttpRequest build() {
            if (url.isBlank()) throw new IllegalStateException("url is required");
            if (method.equals("GET") && body != null) throw new IllegalStateException("a GET request cannot have a body");
            if (retries < 0) throw new IllegalStateException("retries cannot be negative");
            if (timeoutSeconds <= 0) throw new IllegalStateException("timeout must be positive");
            return new HttpRequest(this);
        }
    }
}

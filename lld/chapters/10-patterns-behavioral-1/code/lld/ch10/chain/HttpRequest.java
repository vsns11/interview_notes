package lld.ch10.chain;

import java.util.Map;

/** Minimal HTTP request for the servlet-style demo. */
record HttpRequest(String path, Map<String, String> headers) {}

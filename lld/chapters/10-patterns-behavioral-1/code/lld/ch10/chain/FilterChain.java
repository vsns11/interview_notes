package lld.ch10.chain;

import java.util.List;

/** Walks the filter list. One FilterChain object per request, because it holds a position. */
final class FilterChain {
    private final List<Filter> filters;
    private final Endpoint endpoint;
    private int position = 0;

    FilterChain(List<Filter> filters, Endpoint endpoint) {
        this.filters = filters;
        this.endpoint = endpoint;
    }

    void doFilter(HttpRequest request, StringBuilder response) {
        if (position < filters.size()) {
            filters.get(position++).doFilter(request, response, this);
        } else {
            endpoint.service(request, response);   // the terminal handler
        }
    }
}

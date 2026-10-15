package lld.ch11.architecture.pipeline;

/**
 * PIPE-AND-FILTER: a filter turns one input into one output (or rejects it).
 * then() is the PIPE: it feeds this filter's output into the next filter.
 * Filters never call each other directly, so they can be reordered, reused and tested alone.
 */
@FunctionalInterface
public interface Filter<I, O> {
    Result<O> process(I input);

    static <I, O> Filter<I, O> of(Filter<I, O> filter) { return filter; }

    default <R> Filter<I, R> then(Filter<O, R> next) {
        return input -> switch (process(input)) {
            case Result.Ok<O> ok -> next.process(ok.value());
            case Result.Rejected<O> rejected -> new Result.Rejected<>(rejected.reason());
        };
    }
}

package lld.ch11.architecture.pipeline;

/** What a filter emits: a value for the next filter, or a rejection that ends the run. */
public sealed interface Result<T> permits Result.Ok, Result.Rejected {
    record Ok<T>(T value) implements Result<T> {}
    record Rejected<T>(String reason) implements Result<T> {}
}

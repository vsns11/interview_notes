package ch17.v23;

/// JEP 467 (Java 23): documentation comments written in **Markdown**.
///
/// Lines start with `///` instead of the `/** ... */` block, and use Markdown instead of HTML:
///
/// - lists like this one
/// - `inline code` and **bold**
/// - links to program elements: [String#strip()]
///
/// @param <T> the element type (block tags such as `@param` still work)
public class MarkdownDoc<T> {

    /// Creates a helper.
    public MarkdownDoc() {}

    /// Returns the first non-null argument, or `null` if there is none.
    ///
    /// @param a first choice
    /// @param b fallback
    /// @return `a` if it is not `null`, otherwise `b`
    public T firstNonNull(T a, T b) { return a != null ? a : b; }

    /// Demo entry point.
    ///
    /// @param args ignored
    public static void main(String[] args) {
        System.out.println(new MarkdownDoc<String>().firstNonNull(null, "fallback"));
    }
}

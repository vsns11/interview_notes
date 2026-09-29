package lld.ch04.lsp.shapes;

/** The fix for Rectangle/Square: promise only what every shape can keep. */
public sealed interface Shape permits Rect, Sq {
    int area();
}

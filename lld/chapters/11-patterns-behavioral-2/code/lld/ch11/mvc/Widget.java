package lld.ch11.mvc;

import java.util.List;
import java.util.function.Supplier;

/** COMPOSITE: the view is a tree of widgets rendered the same way. */
public interface Widget {
    void render(StringBuilder out, String indent);

    record Label(Supplier<String> text) implements Widget {
        public void render(StringBuilder out, String indent) { out.append(indent).append(text.get()).append('\n'); }
    }

    record Panel(String title, List<Widget> children) implements Widget {
        public void render(StringBuilder out, String indent) {
            out.append(indent).append("+ ").append(title).append('\n');
            children.forEach(c -> c.render(out, indent + "  "));
        }
    }
}

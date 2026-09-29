package lld.ch10.mediator;

import java.util.List;

final class MethodSelect extends Component {
    private List<String> options = List.of();
    private String selected;

    void setOptions(List<String> options) {
        this.options = options;
        this.selected = options.isEmpty() ? null : options.get(0);
        System.out.println("  [methods] reloaded: " + options + (selected == null ? "" : ", default " + selected));
    }

    void select(String method) {
        if (!options.contains(method)) throw new IllegalArgumentException(method + " not offered");
        selected = method;
        System.out.println("  [methods] user picked " + method);
        mediator.notify(this, method);
    }

    String selected() { return selected; }
}

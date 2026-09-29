package lld.ch10.mediator;

final class SubmitButton extends Component {
    private boolean enabled;

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println("  [submit] " + (enabled ? "enabled" : "disabled"));
    }

    void click() {
        if (enabled) mediator.notify(this, "click");
        else System.out.println("  [submit] click ignored: button is disabled");
    }
}

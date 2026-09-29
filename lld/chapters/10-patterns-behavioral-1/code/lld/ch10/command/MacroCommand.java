package lld.ch10.command;

import java.util.List;

/** A command made of commands. Runs in order; undoes in REVERSE order. */
final class MacroCommand implements UndoableCommand {
    private final String label;
    private final List<UndoableCommand> steps;

    MacroCommand(String label, List<UndoableCommand> steps) {
        this.label = label;
        this.steps = List.copyOf(steps);
    }

    @Override public void execute() { steps.forEach(Command::execute); }

    @Override
    public void undo() {
        for (int i = steps.size() - 1; i >= 0; i--) steps.get(i).undo();
    }

    @Override public String name() { return "Macro:" + label; }
}

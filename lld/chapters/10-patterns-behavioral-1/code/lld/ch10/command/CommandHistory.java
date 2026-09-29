package lld.ch10.command;

import java.util.ArrayDeque;
import java.util.Deque;

/** The INVOKER: runs commands and remembers them. Knows nothing about carts. */
public final class CommandHistory {
    private final Deque<UndoableCommand> undoStack = new ArrayDeque<>();
    private final Deque<UndoableCommand> redoStack = new ArrayDeque<>();

    public void run(Command command) {
        command.execute();
        if (command instanceof UndoableCommand u) {
            undoStack.push(u);
            redoStack.clear();     // a new action destroys the "future" you could have redone
            System.out.println("  ran  " + command.name());
        } else {
            System.out.println("  ran  " + command.name() + " (irreversible: kept out of history)");
        }
    }

    public void undo() {
        if (undoStack.isEmpty()) { System.out.println("  nothing to undo"); return; }
        UndoableCommand c = undoStack.pop();
        c.undo();
        redoStack.push(c);
        System.out.println("  undo " + c.name());
    }

    public void redo() {
        if (redoStack.isEmpty()) { System.out.println("  nothing to redo"); return; }
        UndoableCommand c = redoStack.pop();
        c.execute();
        undoStack.push(c);
        System.out.println("  redo " + c.name());
    }
}

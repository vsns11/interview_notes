package lld.ch10.command;

/** Only commands that can honestly be reversed implement this. */
public interface UndoableCommand extends Command {
    void undo();
}

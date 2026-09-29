package lld.ch10.command;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Cart cart = new Cart();
        CommandHistory history = new CommandHistory();

        System.out.println("=== 1. Undo / redo ===");
        cart.setDiscount(5);   // the cart does NOT start at 0%: a guessing undo would be wrong
        history.run(new AddItemCommand(cart, "widget", 2));
        history.run(new AddItemCommand(cart, "gadget", 1));
        history.run(new ApplyDiscountCommand(cart, 10));
        System.out.println("  " + cart);
        history.undo();
        history.undo();
        System.out.println("  " + cart + "   <- discount restored to the recorded 5%, not 0%");
        history.redo();
        System.out.println("  " + cart);
        history.run(new AddItemCommand(cart, "cable", 3));   // clears the redo stack
        history.redo();

        System.out.println("\n=== 2. Macro command ===");
        UndoableCommand starterKit = new MacroCommand("starter-kit", List.of(
            new AddItemCommand(cart, "keyboard", 1),
            new AddItemCommand(cart, "mouse", 1),
            new ApplyDiscountCommand(cart, 15)));
        history.run(starterKit);
        System.out.println("  " + cart);
        history.undo();                                      // one undo reverses all three steps
        System.out.println("  " + cart);

        System.out.println("\n=== 3. Irreversible command (no undo offered) ===");
        Command sendReceipt = new Command() {
            @Override public void execute() { System.out.println("  [email] receipt sent for " + cart); }
            @Override public String name()  { return "SendReceipt"; }
        };
        history.run(sendReceipt);

        System.out.println("\n=== 4. Commands on a queue: Runnable + ExecutorService (the invoker) ===");
        List<Runnable> jobs = List.of(
            () -> System.out.println("  [worker] resize image 1"),
            () -> System.out.println("  [worker] resize image 2"),
            () -> System.out.println("  [worker] send newsletter"));
        System.out.println("  queued " + jobs.size() + " jobs");
        ExecutorService worker = Executors.newSingleThreadExecutor();   // one thread => FIFO order
        jobs.forEach(worker::submit);
        worker.shutdown();
        worker.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("  all jobs done");
    }
}

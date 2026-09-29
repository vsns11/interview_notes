package lld.ch06.names;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Meaningful names: the same logic, before and after renaming. */
public class Main {

    // ---------------- BEFORE ----------------
    static class Before {
        static class DtaRcrd102 {                      // unpronounceable, encoded, meaningless
            String strNm; int i1; int i2; boolean flg;
        }
        static class Mgr {                              // vague noun
            List<DtaRcrd102> lst = new ArrayList<>();   // "lst" of what?
            // what is 4? what is 18000? why flg?
            List<DtaRcrd102> getThem() {
                List<DtaRcrd102> l1 = new ArrayList<>();
                for (DtaRcrd102 x : lst) if (x.i1 == 4 && x.i2 > 18000 && !x.flg) l1.add(x);
                return l1;
            }
            int d;                                       // elapsed time in days? seconds? since what?
        }
    }

    // ---------------- AFTER ----------------
    static class After {
        enum TaskStatus { OPEN, IN_PROGRESS, BLOCKED, DONE }

        /** Class name = noun. Field names = what they hold, with units where it matters. */
        record Task(String title, TaskStatus status, Duration timeSpent, boolean archived) {
            boolean isCompleted() { return status == TaskStatus.DONE; }           // boolean method reads as a question
            boolean tookLongerThan(Duration limit) { return timeSpent.compareTo(limit) > 0; }
        }

        static final class TaskBoard {
            private static final Duration LONG_RUNNING = Duration.ofHours(5);  // named constant, not a magic number
            private final List<Task> tasks = new ArrayList<>();
            void add(Task task) { tasks.add(task); }                             // method name = verb
            List<Task> findCompletedLongRunningTasks() {                         // says exactly what it returns
                List<Task> result = new ArrayList<>();
                for (Task task : tasks)
                    if (task.isCompleted() && task.tookLongerThan(LONG_RUNNING) && !task.archived()) result.add(task);
                return result;
            }
        }

        /** Static factory methods with descriptive names beat overloaded constructors. */
        record Temperature(double kelvin) {
            static Temperature ofCelsius(double c) { return new Temperature(c + 273.15); }
            static Temperature ofFahrenheit(double f) { return new Temperature((f - 32) * 5 / 9 + 273.15); }
        }
    }

    public static void main(String[] args) {
        var mgr = new Before.Mgr();
        var r = new Before.DtaRcrd102(); r.strNm = "Deploy"; r.i1 = 4; r.i2 = 20000; mgr.lst.add(r);
        System.out.println("BEFORE getThem(): " + mgr.getThem().size() + " record(s) -- but what are they?");

        var board = new After.TaskBoard();
        board.add(new After.Task("Deploy", After.TaskStatus.DONE, Duration.ofHours(6), false));
        board.add(new After.Task("Write docs", After.TaskStatus.DONE, Duration.ofHours(1), false));
        System.out.println("AFTER findCompletedLongRunningTasks(): " + board.findCompletedLongRunningTasks());
        System.out.printf("Temperature.ofCelsius(25) = %.2f K, ofFahrenheit(212) = %.2f K%n",
                After.Temperature.ofCelsius(25).kelvin(), After.Temperature.ofFahrenheit(212).kelvin());
    }
}

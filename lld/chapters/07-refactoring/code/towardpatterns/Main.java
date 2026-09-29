package lld.ch07.towardpatterns;

/** Refactoring toward patterns: each demo runs the BEFORE and AFTER code and checks they agree. */
public class Main {
    public static void main(String[] args) {
        boolean ok = StrategyRefactoring.demo()
                & StateRefactoring.demo()
                & SpecialCaseRefactoring.demo()
                & TemplateMethodRefactoring.demo()
                & FactoryRefactoring.demo();
        System.out.println(ok ? "All pattern refactorings preserve behaviour." : "BEHAVIOUR CHANGED!");
        if (!ok) System.exit(1);
    }
}

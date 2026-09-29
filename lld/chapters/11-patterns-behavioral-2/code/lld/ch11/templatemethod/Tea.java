package lld.ch11.templatemethod;

public final class Tea extends CaffeineBeverage {
    @Override protected void brew() { System.out.println("  steeping the tea"); }
    @Override protected void addCondiments() { System.out.println("  adding lemon"); }
}

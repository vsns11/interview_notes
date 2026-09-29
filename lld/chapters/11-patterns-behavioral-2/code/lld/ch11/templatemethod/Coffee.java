package lld.ch11.templatemethod;

public final class Coffee extends CaffeineBeverage {
    private final boolean black;

    public Coffee(boolean black) { this.black = black; }

    @Override protected void brew() { System.out.println("  dripping coffee through filter"); }
    @Override protected void addCondiments() { System.out.println("  adding sugar and milk"); }
    @Override protected boolean customerWantsCondiments() { return !black; }
}

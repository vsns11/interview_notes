package lld.ch11.templatemethod;

/** The classic beverage example: same recipe skeleton, two steps and one hook vary. */
public abstract class CaffeineBeverage {
    public final void prepareRecipe() {          // template method
        boilWater();
        brew();
        pourInCup();
        if (customerWantsCondiments()) addCondiments();
    }

    protected abstract void brew();
    protected abstract void addCondiments();

    /** Hook: "Don't call us, we'll call you" - the base class decides WHEN this runs. */
    protected boolean customerWantsCondiments() { return true; }

    private void boilWater() { System.out.println("  boiling water"); }
    private void pourInCup() { System.out.println("  pouring into cup"); }
}

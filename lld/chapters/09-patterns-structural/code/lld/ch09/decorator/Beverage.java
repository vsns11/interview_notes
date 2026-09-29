package lld.ch09.decorator;

/** A second, classic decorator example: a coffee order priced by stacking add-ons.
 *  Prices are whole cents (int), so no floating-point money anywhere. */
interface Beverage {
    String description();
    int costCents();
}

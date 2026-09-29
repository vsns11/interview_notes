package lld.ch10.mediator;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Shipping form: widgets coordinated by a mediator ===");
        CountrySelect country = new CountrySelect();
        MethodSelect methods = new MethodSelect();
        PriceLabel price = new PriceLabel();
        SubmitButton submit = new SubmitButton();
        new ShippingFormMediator(country, methods, price, submit);

        country.select("Germany");        // reloads methods, shows price, enables submit
        methods.select("express");        // updates price only
        submit.click();
        country.select("Antarctica");     // no methods: price hidden, submit disabled
        submit.click();

        System.out.println("\n=== 2. Chat room ===");
        ChatRoom room = new ChatRoom();
        ChatUser asha = new ChatUser("asha"), ben = new ChatUser("ben"), chen = new ChatUser("chen");
        room.join(asha);
        room.join(ben);
        room.join(chen);
        asha.say("standup in 5");
        ben.whisper("chen", "coffee first?");
        chen.say("buy cheap SPAM now");

        System.out.println("\n=== 3. Air traffic control ===");
        ControlTower tower = new ControlTower();
        Flight ai101 = new Flight("AI101", tower), ba22 = new Flight("BA22", tower), lh7 = new Flight("LH7", tower);
        ai101.requestLanding();
        ba22.requestLanding();
        lh7.requestLanding();
        ai101.landed();
        ba22.landed();
    }
}

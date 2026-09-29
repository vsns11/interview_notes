package lld.ch11.state;

final class HasMoney implements VendingState {
    public String name() { return "has_money"; }

    @Override
    public void insertCoin(VendingMachine m, int cents) { m.addBalance(cents); }

    @Override
    public void select(VendingMachine m, String code) {
        if (!m.inStock(code)) { System.out.println("  [has_money] " + code + " is empty, choose another"); return; }
        int price = m.priceOf(code);
        if (m.balance() < price) {
            System.out.println("  [has_money] " + code + " costs " + price + ", balance " + m.balance());
            return;
        }
        m.dispense(code);                                 // business action lives in the context
        m.returnChange();
        m.setState(m.anythingInStock() ? new Idle() : new SoldOut());
    }

    @Override
    public void cancel(VendingMachine m) {
        m.returnChange();
        m.setState(new Idle());
    }
}

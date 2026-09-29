package lld.ch11.state;

final class Idle implements VendingState {
    public String name() { return "idle"; }

    @Override
    public void insertCoin(VendingMachine m, int cents) {
        m.addBalance(cents);
        m.setState(new HasMoney());
    }
}

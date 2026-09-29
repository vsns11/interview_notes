package lld.ch11.state;

final class SoldOut implements VendingState {
    public String name() { return "sold_out"; }        // every action uses the refusing defaults
}

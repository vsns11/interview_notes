package lld.ch09.proxy;

final class RealDevice implements Device {
    RealDevice() { System.out.println("    [device] opened (expensive)"); }
    @Override public String query(String command) { return "ok:" + command; }
    @Override public void close() { System.out.println("    [device] closed"); }
}

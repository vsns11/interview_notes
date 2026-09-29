package lld.ch02.bank;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank("Gurus Bank");
        bank.add(new SavingsAccount("S-1", "Ada", 50_000, 25));
        bank.add(new CheckingAccount("C-1", "Kai", 8_000, 250, 50_000));
        bank.add(new Account("P-1", "Ravi", 3_000));

        System.out.println(bank.name() + ", opening balances:");
        for (Account a : bank.accounts()) {
            System.out.println("  " + a.id() + " (" + a.owner() + "): " + Money.format(a.balanceCents()));
        }
        System.out.println(bank.transfer("S-1", "C-1", 10_000));
        System.out.println(bank.transfer("S-1", "C-1", 90_000));
        System.out.println("month end:");
        bank.monthEnd().forEach(line -> System.out.println("  " + line));
    }
}

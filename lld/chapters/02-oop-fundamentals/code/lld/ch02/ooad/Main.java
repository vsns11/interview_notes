package lld.ch02.ooad;

public class Main {
    public static void main(String[] args) {
        Library lib = new Library("City Library");
        Book ddd = new Book("978-0321125217", "Domain-Driven Design");
        Book gof = new Book("978-0201633610", "Design Patterns");
        lib.addBook(ddd);
        lib.addBook(gof);
        Member ada = new Member("M1", "Ada");
        Member kai = new Member("M2", "Kai");
        lib.register(ada);
        lib.register(kai);

        System.out.println("Ada borrows DDD on day 1: " + lib.lend(ddd, ada, 1).map(l -> "due day " + l.dueDay()).orElse("refused"));
        System.out.println("Kai borrows DDD on day 2: " + lib.lend(ddd, kai, 2).map(l -> "due day " + l.dueDay()).orElse("refused (already out)"));
        System.out.println("Kai borrows GoF on day 2: " + lib.lend(gof, kai, 2).map(l -> "due day " + l.dueDay()).orElse("refused"));
        System.out.println("is DDD out? " + lib.isOut(ddd) + ", Ada's loans: " + ada.loanCount());
        System.out.println(lib.takeBack(ddd, 10));
        System.out.println(lib.takeBack(gof, 20));
        System.out.println(lib.takeBack(gof, 21));
        System.out.println("is DDD out? " + lib.isOut(ddd));
    }
}

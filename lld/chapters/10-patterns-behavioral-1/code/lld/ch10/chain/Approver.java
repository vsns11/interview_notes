package lld.ch10.chain;

import java.math.BigDecimal;

/**
 * Approval chain. Pure Chain of Responsibility: the FIRST handler able to deal
 * with the request handles it, and nobody after it sees the request.
 * Note there is only one class: the chain is data (name + limit), not code.
 */
final class Approver {
    private final String role;
    private final BigDecimal limitUsd;
    private Approver next;

    Approver(String role, BigDecimal limitUsd) {
        this.role = role;
        this.limitUsd = limitUsd;
    }

    Approver then(Approver next) {
        this.next = next;
        return next;
    }

    String approve(Expense e) {
        if (e.amountUsd().compareTo(limitUsd) <= 0) {   // compareTo, not equals: 300 and 300.00 are the same amount
            return role + " approved " + e.id() + " ($" + e.amountUsd() + ")";
        }
        if (next == null) {   // explicit terminal outcome instead of a silent "nothing happened"
            return "REJECTED " + e.id() + " ($" + e.amountUsd() + "): above every approver's limit";
        }
        System.out.println("    " + role + " (limit $" + limitUsd + ") escalates " + e.id());
        return next.approve(e);
    }
}

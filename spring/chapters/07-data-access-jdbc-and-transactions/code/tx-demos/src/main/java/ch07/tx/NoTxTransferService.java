package ch07.tx;

import org.springframework.stereotype.Service;

/** WRONG layer: the use case (the transfer) has no transaction; each DAO call commits on its own. */
@Service
public class NoTxTransferService {

    private final AccountDao dao;

    public NoTxTransferService(AccountDao dao) {
        this.dao = dao;
    }

    public void transfer(String from, String to, long amount) {
        dao.add(to, amount);    // commits immediately
        dao.add(from, -amount); // may fail -> the credit above is NOT undone
    }
}

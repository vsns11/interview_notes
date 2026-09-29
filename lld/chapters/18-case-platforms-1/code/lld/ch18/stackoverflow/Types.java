package lld.ch18.stackoverflow;

/** Lifecycle of a question. ON_HOLD is the older name Stack Overflow used for a freshly closed question. */
enum QuestionStatus { OPEN, ON_HOLD, CLOSED, DELETED }

/** Why a question was closed. */
enum ClosingRemark {
    DUPLICATE, OFF_TOPIC, TOO_BROAD, NOT_CONSTRUCTIVE, NOT_A_REAL_QUESTION, PRIMARILY_OPINION_BASED
}

enum AccountStatus { ACTIVE, CLOSED, CANCELED, BLACKLISTED, BLOCKED }

enum VoteType { UP, DOWN }

/**
 * Reputation needed to unlock an action. The numbers follow the public
 * privileges page of stackoverflow.com, so "reputation unlocks privileges"
 * is data here, not if-statements spread over the code.
 */
enum Privilege {
    ASK_OR_ANSWER(1),
    VOTE_UP(15),
    FLAG(15),
    COMMENT_EVERYWHERE(50),
    SET_BOUNTY(75),
    VOTE_DOWN(125),
    CREATE_TAG(1500),
    EDIT_ANY_POST(2000),
    CAST_CLOSE_VOTE(3000),
    CAST_DELETE_VOTE(10000);

    final int minReputation;

    Privilege(int minReputation) { this.minReputation = minReputation; }
}

/** A badge is just a name and a description; members own the badges they earn. */
record Badge(String name, String description) {}

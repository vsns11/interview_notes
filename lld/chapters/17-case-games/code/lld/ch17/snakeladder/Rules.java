package lld.ch17.snakeladder;

/**
 * The rules engine's configuration, bundled so a game variant is one object.
 * sixRollsAgain: rolling the dice's maximum earns another roll; maxBonusRolls in a row cancels the turn.
 */
public record Rules(OverflowRule overflow, WinStrategy win, boolean sixRollsAgain, int maxBonusRolls) {
    public static Rules classic() { return new Rules(OverflowRule.exactFinish(), WinStrategy.firstToFinish(), true, 3); }
}

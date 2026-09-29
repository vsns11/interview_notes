package lld.ch21.codingblox;

import java.util.List;

public class Main {
    static final List<String> SAMPLE_INPUT = List.of(
            "CreateUser Ross", "CreateUser Monica", "CreateUser Joey", "CreateUser Chandler",
            "CreateQuestion \"LOW\" 10", "CreateQuestion LOW 20", "CreateQuestion LOW 30",
            "CreateQuestion MEDIUM 40", "CreateQuestion MEDIUM 50", "CreateQuestion HIGH 100",
            "ListQuestion LOW",
            "CreateContest \"diwali_contest\" LOW \"Ross\"",
            "CreateContest holi_contest MEDIUM Monica",
            "ListContest",
            "AttendContest 1 Monica", "AttendContest 1 Joey", "AttendContest 1 Chandler",
            "AttendContest 1 Joey",                  // duplicate
            "WithdrawContest 1 Chandler",
            "WithdrawContest 1 Ross",                // creator cannot withdraw
            "RunContest 1 Monica",                   // not the creator
            "RunContest 1 Ross",
            "RunContest 1 Ross",                     // already run
            "AttendContest 1 Chandler",              // too late
            "ContestHistory 1",
            "AttendContest 2 Chandler",
            "RunContest 2 Monica",
            "LeaderBoard score desc",
            "LeaderBoard asc",
            "CreateContest x HIGH Rachel",           // unknown user
            "Dance 1");                              // unknown command

    public static void main(String[] args) {
        CommandProcessor cli = new CommandProcessor(new CodingBloxService(new RandomSolveSimulator(42)));
        SAMPLE_INPUT.forEach(cli::execute);
    }
}

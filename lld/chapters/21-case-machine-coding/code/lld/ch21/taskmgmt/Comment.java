package lld.ch21.taskmgmt;

import java.time.LocalDateTime;

public record Comment(String authorId, String text, LocalDateTime at) { }

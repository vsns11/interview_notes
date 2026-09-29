package lld.ch02.generics;

public record User(String id, String name) implements Identifiable<String> { }

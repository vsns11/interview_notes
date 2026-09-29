package lld.ch11.repository;

/** A domain object. It knows nothing about storage. */
public record User(String id, String email, boolean active) {}

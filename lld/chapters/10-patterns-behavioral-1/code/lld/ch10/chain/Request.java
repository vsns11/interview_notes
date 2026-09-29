package lld.ch10.chain;

/** The request that travels down the API pipeline. */
public record Request(String user, String token, String payload) {}

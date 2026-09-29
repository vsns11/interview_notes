package lld.ch09.proxy;

/** SUBJECT interface: callers only ever hold this type. */
interface Report {
    String title();
    String render();
}

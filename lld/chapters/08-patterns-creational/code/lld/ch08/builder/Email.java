package lld.ch08.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Staged (step) builder: each step returns an interface exposing only the legal next calls,
 * so the compiler forces "to" and "subject" before build() is even visible.
 */
public record Email(String to, String subject, String body, List<String> cc) {

    public Email { cc = List.copyOf(cc); }                    // records + builder = immutable product

    public interface ToStep      { SubjectStep to(String address); }
    public interface SubjectStep { OptionalStep subject(String subject); }
    public interface OptionalStep {
        OptionalStep body(String body);
        OptionalStep cc(String address);
        Email build();
    }

    public static ToStep builder() { return new Steps(); }

    private static final class Steps implements ToStep, SubjectStep, OptionalStep {
        private String to, subject, body = "";
        private final List<String> cc = new ArrayList<>();
        public SubjectStep to(String a)       { this.to = a; return this; }
        public OptionalStep subject(String s) { this.subject = s; return this; }
        public OptionalStep body(String b)    { this.body = b; return this; }
        public OptionalStep cc(String a)      { cc.add(a); return this; }
        public Email build()                  { return new Email(to, subject, body, cc); }
    }
}

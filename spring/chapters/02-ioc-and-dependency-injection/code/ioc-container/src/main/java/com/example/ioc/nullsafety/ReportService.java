package com.example.ioc.nullsafety;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    private final @Nullable Translator translator;   // may be absent

    // Spring treats a parameter annotated with any @Nullable as optional: it injects null if no bean.
    public ReportService(@Nullable Translator translator) {
        this.translator = translator;
    }

    public String title(String raw) {
        return translator == null ? raw : translator.translate(raw);   // the null check the annotation asks for
    }

    public @Nullable String findTitle(int id) {       // callers can see null is possible
        return id == 1 ? "Q3 sales" : null;
    }
}

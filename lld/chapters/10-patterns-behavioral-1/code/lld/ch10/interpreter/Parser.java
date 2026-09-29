package lld.ch10.interpreter;

/**
 * Recursive-descent parser: turns text into an Abstract Syntax Tree (AST).
 * Parsing is NOT part of the GoF Interpreter pattern; the pattern starts once the tree exists.
 * One method per grammar rule, mirroring the grammar in Expression.
 */
public final class Parser {
    private final String src;
    private int pos = 0;

    private Parser(String src) { this.src = src; }

    public static Expression parse(String text) {
        Parser p = new Parser(text);
        Expression e = p.expr();
        p.skipSpaces();
        if (p.pos != p.src.length()) throw p.error("unexpected '" + p.src.charAt(p.pos) + "'");
        return e;
    }

    private Expression expr() {
        Expression left = term();
        while (peek('+') || peek('-')) {
            char op = src.charAt(pos++);
            left = new Expression.Binary(op, left, term());   // left-associative
        }
        return left;
    }

    private Expression term() {
        Expression left = factor();
        while (peek('*') || peek('/')) {
            char op = src.charAt(pos++);
            left = new Expression.Binary(op, left, factor());
        }
        return left;
    }

    private Expression factor() {
        skipSpaces();
        if (pos >= src.length()) throw error("unexpected end of input");
        char c = src.charAt(pos);
        if (c == '-') { pos++; return new Expression.Negate(factor()); }
        if (c == '(') {
            pos++;
            Expression inner = expr();
            if (!peek(')')) throw error("missing ')'");
            pos++;
            return inner;
        }
        int start = pos;
        if (Character.isDigit(c) || c == '.') {
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) pos++;
            return new Expression.Constant(Double.parseDouble(src.substring(start, pos)));
        }
        if (Character.isLetter(c)) {
            while (pos < src.length() && Character.isLetterOrDigit(src.charAt(pos))) pos++;
            return new Expression.Variable(src.substring(start, pos));
        }
        throw error("unexpected '" + c + "'");
    }

    private boolean peek(char expected) {
        skipSpaces();
        return pos < src.length() && src.charAt(pos) == expected;
    }

    private void skipSpaces() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    private IllegalArgumentException error(String msg) {
        return new IllegalArgumentException(msg + " at position " + pos + " in \"" + src + "\"");
    }
}

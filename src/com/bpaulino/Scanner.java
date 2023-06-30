package com.bpaulino;

import com.bpaulino.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL: TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                // Comments start with '//' and go until the end of the line
                if (match('/')) {
                    // check the follow-up characters until the end of the line.
                    // keep advancing until we reach a line break.
                    while(peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            // Ignore all whitespace variations
            case ' ':
            case '\r':
            case '\t':
                break;
            // Account for which line we are at so it's easier to report errors
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                    // Identifiers need to start with letter only
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // Report the invalid character, but keep scanning.
                    // Let's report more errors to the users if we find them.
                    Lox.error(line, "Unexpected character");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphanumeric(peek())) {
            advance();
        }

        // make sure that this identifier does not match reserved words
        String identifier = source.substring(start, current);
        TokenType type = keywords.get(identifier);
        // Fallback to a variable identifier otherwise
        if (type == null) {
            type = TokenType.IDENTIFIER;
        }

        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        // Consume the first integer part
        while(isDigit(peek())) {
            advance();
        }

        // Look for fractions
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the '.'
            advance();
            // keep consuming all digits after the decimal place
            while(isDigit(peek())) advance();
        }

        Double number = Double.parseDouble(source.substring(start, current));
        addToken(TokenType.NUMBER, number);
    }

    private char peekNext() {
        // double-check source boundaries
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private void string() {
        // Consume the string until the character before the double quotes
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        // If we reach the end of the content at this point,
        // it means that a string was never terminated with a double quote.
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return;
        }

        // consume the closing double quotes
        advance();

        // At this point, we have the ranges of the string
        // including its double-quotes, so we need to discard
        // the quotes and just store the literal value.
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private char advance() {
        return source.charAt(current++);
    }

    // Take a look at the next character to be consumed.
    // But do not consume. (lookahead)
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // Check if the next character matches the given char.
    // If that is the case, consume it.
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private void addToken(TokenType token) {
        addToken(token, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}

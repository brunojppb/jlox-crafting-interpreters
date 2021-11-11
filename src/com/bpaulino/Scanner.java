package com.bpaulino;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  // offsets from the source string
  // `start` points to the first character in the lexeme being scanned
  // e.g.:
  // var name
  //     ^
  private int start = 0;
  // `current` points to the current character being analyzed
  // e.g.:
  // var name
  //      ^
  // where `n` was the start of this lexeme and `a` is the current character being inspected
  private int current = 0;
  // `line` points to the line number where the `current` character is from
  // This allows to keep track of the token location.
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while(!isAtEnd()) {
      start = current;
      scanToken();
    }

    // Done parsing source. Append EOF
    // to signal that we finished parsing
    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  // Get the current character and advance to the next one.
  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  // Lookahead for what comes next in the lexeme.
  // (Notice that it doesn't consume the character)
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  // take a peek at the second character after the current
  // (if the source string allows it)
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  private void string() {
    while(peek() != '"' && !isAtEnd()) {
      if(peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    // We reached the closing quotes '"'
    advance();

    // Trim the surrounding quotes and get the string content
    String value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  // consume the current number (Always double precision numbers)
  private void number() {
    while(isDigit(peek())) {
      advance();
    }

    // Check for decimal places. a '.' should be followed by an integer
    if (peek() == '.' && isDigit(peekNext())) {
      // consume the '.' and move on
      advance();

      while(isDigit(peek())) {
        advance();
      }
    }

    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void scanToken() {
    char c = advance();
    // Let's start with the lexemes that are one character long,
    // so it's a 1 to 1 conversion to a token.
    switch (c) {
      case '(' -> addToken(TokenType.LEFT_PAREN);
      case ')' -> addToken(TokenType.RIGHT_PAREN);
      case '{' -> addToken(TokenType.LEFT_BRACE);
      case '}' -> addToken(TokenType.RIGHT_BRACE);
      case ',' -> addToken(TokenType.COMMA);
      case '.' -> addToken(TokenType.DOT);
      case '-' -> addToken(TokenType.MINUS);
      case '+' -> addToken(TokenType.PLUS);
      case ';' -> addToken(TokenType.SEMICOLON);
      case '*' -> addToken(TokenType.STAR);

      // From here we have cases where a lexeme can be composed by one or more characters
      // '!' and '!=' are different operators, but both start with '!'.
      // So we have to look ahead to check what comes next.
      case '!' -> addToken(match('=') ? TokenType.EQUAL : TokenType.BANG);
      case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
      case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
      case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);

      // Let's handle the '/' (division) operator. Remember that comments
      // start with a '/' too, so we need to account for that.
      case '/' -> {
        if (match('/')) {
          while(peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(TokenType.SLASH);
        }
      }

      // Skip whitespace
      case ' ', '\r', '\t' -> {}

      // Advance to the next line
      case '\n' -> line++;

      case '"' -> string();

      default -> {
        if (isDigit(c)) {
          number();
        } else {
          Lox.error(line, "Unexpected Character");
        }
      }

    }
  }

}

























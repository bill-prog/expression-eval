package com.example.expressioneval.analysis;

import com.example.expressioneval.exception.exceptions.TokenizerException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;

@Component
public class Tokenizer {

    public Tokenizer() {}

    public enum TokenType {
        T_AND("\\bAND\\b"),
        T_OR("\\bOR\\b"),
        T_NULL("\\bnull\\b"),
        T_TRUE("\\btrue\\b"),
        T_FALSE("\\bfalse\\b"),
        T_VAR("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*"),
        T_LPAREN("\\("),
        T_RPAREN("\\)"),
        T_EQ("=="),
        T_LEQ("<="),
        T_GEQ(">="),
        T_NEQ("!="),
        T_LT("<"),
        T_GT(">"),
        T_STRING("\"[^\"]*\""),
        T_NUM("\\d+"),
        T_WHITESPACE("\\s+");

        public final Pattern pattern;

        TokenType(String regex) {
            this.pattern = Pattern.compile("^" + regex);
        }
    }


    public ArrayList<Token> tokenize(String inputString) {
        ArrayList<Token> tokens = new ArrayList<>();
        String s = inputString;
        int currentPosition = 0;

        while (!s.isEmpty()) {
            boolean match = false;

            for (TokenType tt : TokenType.values()) {
                Matcher m = tt.pattern.matcher(s);
                if (m.find()) {
                    match = true;
                    String tok = m.group().trim();
                    if(tt != TokenType.T_WHITESPACE) { // Ignore whitespace
                        tokens.add(new Token(tt, tok));
                    }
                    s = m.replaceFirst("");
                    break;
                }
            }

            if (!match) {
                throw new TokenizerException("Unexpected character sequence in input starting at position: " + currentPosition);
            }
            currentPosition += s.length();
        }

        return tokens;
    }

    public static class Token {
        TokenType type;
        String data;

        public Token(TokenType type, String data) {
            this.type = type;
            if (type == TokenType.T_STRING) {
                this.data = data.substring(1, data.length() - 1);
            } else {
                this.data = data;
            }
        }

        @Override
        public String toString() {
            return String.format("(%s, %s)", type.name(), data);
        }

        public TokenType getType() {
            return type;
        }

        public void setType(TokenType type) {
            this.type = type;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

}

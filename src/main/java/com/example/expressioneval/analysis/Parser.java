package com.example.expressioneval.analysis;

import com.example.expressioneval.exception.exceptions.ParseException;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;


// note: comments are a bit verbose, but I kept them as this can be complex to go over
@Component
@Scope("prototype")
public class Parser {

    private ArrayList<Tokenizer.Token> tokens;
    private int index = 0;

    public ASTNode parse(ArrayList<Tokenizer.Token> tokens) {
        this.tokens = tokens;
        this.index = 0;

        ASTNode result = expression();
        if (peek() != null) {
            throw new ParseException("Unexpected token " + peek().type.name() + " at index " + index, index);
        }
        return result;
    }

    private ASTNode expression() {
        return binaryOperation(this::term, Tokenizer.TokenType.T_OR, "OR"); // expression (OR) dissolves into term (AND)
    }

    private ASTNode term() {
        return binaryOperation(this::comparison, Tokenizer.TokenType.T_AND, "AND"); // term (AND) dissolves into comparison (==, !=, ...)
    }

    private ASTNode binaryOperation(ParserFunction nextFunction, Tokenizer.TokenType tokenType, String operand) {
        ASTNode node = nextFunction.invoke(); // either call term() or comparison()
        while (consume(tokenType) != null) {
            validateOperand(node, operand); // validate the operation
            ASTNode rightNode = nextFunction.invoke(); // left operand is obtained, now find the right node
            validateOperand(rightNode, operand); // validate the operation
            node = new BinaryOpNode(operand, node, rightNode); // we have left and right node, continue if tokenType is still equal
            // to the provided one
        }
        return node;
    }

    private ASTNode comparison() {
        ASTNode node = primary(); // comparison can be done only on primaries (expressions (with parentheses) and basic values)
        while (true) {
            Tokenizer.TokenType nextOp = peekType(); // check next operator
            if (nextOp == null) { // we are done
                return node;
            }
            switch (nextOp) { // try to match the nextOp with available operators
                case T_EQ:
                    node = binaryOpNode("==", node);
                    break;
                case T_NEQ:
                    node = binaryOpNode("!=", node);
                    break;
                case T_LT:
                    node = binaryOpNode("<", node);
                    break;
                case T_LEQ:
                    node = binaryOpNode("<=", node);
                    break;
                case T_GT:
                    node = binaryOpNode(">", node);
                    break;
                case T_GEQ:
                    node = binaryOpNode(">=", node);
                    break;
                default:
                    return node; // if there isn't any operators (==, !=, ...), exit the loop
            }
        }
    }


    private ASTNode binaryOpNode(String op, ASTNode left) {
        consume(peekType()); // consume the operator (==, != ...)
        ASTNode right = primary(); // right operator is primary (either (...) or a plain value)
        if (!areCompatibleForComparison(left, right)) { // compare operands ( e.g. number cant be equal to string)
            throw new ParseException("Incompatible types for comparison at index " + index, index);
        }
        return new BinaryOpNode(op, left, right);
    }

    private ASTNode primary() {
        // primary can either be another expression in ()
        if (consume(Tokenizer.TokenType.T_LPAREN) != null) {
            ASTNode node = expression();
            expect(Tokenizer.TokenType.T_RPAREN);
            return node;
        }

        // or primary can be a simple value
        ASTNode toReturn = value();
        // this is to not allow single instances of strings, numbers and nulls
        if ((toReturn instanceof StringNode || toReturn instanceof NumberNode || toReturn instanceof NullNode)
                && index == 1 && tokens.size() == 1) {
            throw new ParseException("Unexpected token type at index " + index, index);
        }
        return toReturn;
    }

    private ASTNode value() {
        // value cant be dissolved further, return a basic node (variable, boolean, string, number or null)

        Tokenizer.Token t = consume(Tokenizer.TokenType.T_VAR);
        if (t != null) {
            return new ValueNode(t.data);
        }

        if (consume(Tokenizer.TokenType.T_TRUE) != null) {
            return new BooleanNode(true);
        }

        if (consume(Tokenizer.TokenType.T_FALSE) != null) {
            return new BooleanNode(false);
        }

        t = consume(Tokenizer.TokenType.T_STRING);
        if (t != null) {
            return new StringNode(t.data);
        }

        t = consume(Tokenizer.TokenType.T_NUM);
        if (t != null) {
            return new NumberNode(Integer.parseInt(t.data));
        }

        if (consume(Tokenizer.TokenType.T_NULL) != null) {
            return new NullNode();
        }

        throw new ParseException("Unexpected token type at index " + index, index);
    }

    private void validateOperand(ASTNode node, String op) {
        if (isNotValidLogicalOperand(node)) {
            throw new ParseException("Invalid operand for " + op + " at index " + index, index);
        }
    }

    private boolean isNotValidLogicalOperand(ASTNode node) {
        return node instanceof NumberNode || node instanceof StringNode || node instanceof NullNode;
    }

    private boolean areCompatibleForComparison(ASTNode left, ASTNode right) {
        return (left instanceof ValueNode || right instanceof ValueNode)
                || (left instanceof NumberNode && right instanceof NumberNode)
                || (left instanceof StringNode && right instanceof StringNode)
                || (left instanceof BooleanNode && right instanceof BooleanNode)
                || (left instanceof NullNode && right instanceof NullNode);
    }

    private Tokenizer.Token peek() {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    private Tokenizer.Token consume(Tokenizer.TokenType tt) {
        if (index < tokens.size() && tokens.get(index).type == tt) {
            return tokens.get(index++);
        }
        return null;
    }

    private void expect(Tokenizer.TokenType tt) {
        if (consume(tt) == null) {
            throw new ParseException("Expected token of type " + tt.name() + " but found " + peekType(), index);
        }
    }

    private Tokenizer.TokenType peekType() {
        return index < tokens.size() ? tokens.get(index).type : null;
    }

    @FunctionalInterface
    private interface ParserFunction {
        ASTNode invoke();
    }



    // class definitions below this line
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = BinaryOpNode.class, name = "binaryOp"),
            @JsonSubTypes.Type(value = ValueNode.class, name = "value"),
            @JsonSubTypes.Type(value = StringNode.class, name = "string"),
            @JsonSubTypes.Type(value = NumberNode.class, name = "number"),
            @JsonSubTypes.Type(value = NullNode.class, name = "nullNode"),
            @JsonSubTypes.Type(value = BooleanNode.class, name = "booleanNode")
    })
    public static abstract class ASTNode {
    }

    public static class BinaryOpNode extends ASTNode {
        String op;
        ASTNode left, right;

        public BinaryOpNode(String op, ASTNode left, ASTNode right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        public BinaryOpNode() {
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }

        public ASTNode getLeft() {
            return left;
        }

        public void setLeft(ASTNode left) {
            this.left = left;
        }

        public ASTNode getRight() {
            return right;
        }

        public void setRight(ASTNode right) {
            this.right = right;
        }
    }

    public static class ValueNode extends ASTNode {
        String value;

        public ValueNode(String value) {
            this.value = value;
        }

        public ValueNode() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public static class StringNode extends ASTNode {
        String value;

        public StringNode(String value) {
            this.value = value;
        }

        public StringNode() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class NumberNode extends ASTNode {
        int value;

        public NumberNode(int value) {
            this.value = value;
        }

        public NumberNode() {
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class NullNode extends ASTNode {
        public NullNode() {
        }
    }

    public static class BooleanNode extends ASTNode {
        boolean value;

        public BooleanNode(boolean value) {
            this.value = value;
        }

        public BooleanNode() {
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

}


package com.example.expressioneval.analysis;

import com.example.expressioneval.exception.exceptions.EvaluationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class Evaluator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean evaluate(Parser.ASTNode node, String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            return evaluateNode(node, rootNode);
        } catch (JsonProcessingException e) {
            throw new EvaluationException("Failed to parse JSON", e);
        }
    }

    private boolean evaluateNode(Parser.ASTNode node, JsonNode jsonNode) {
        if (node instanceof Parser.BinaryOpNode binaryNode) {
            return switch (binaryNode.getOp()) {
                // for the cases below, we recursively go back to beginning
                case "AND" ->
                        evaluateNode(binaryNode.getLeft(), jsonNode) && evaluateNode(binaryNode.getRight(), jsonNode);
                case "OR" ->
                        evaluateNode(binaryNode.getLeft(), jsonNode) || evaluateNode(binaryNode.getRight(), jsonNode);

                // // for equality and inequality checks, fetch and compare the values
                case "==" ->
                        Objects.equals(getValueFromNode(binaryNode.getLeft(), jsonNode), getValueFromNode(binaryNode.getRight(), jsonNode));
                case "!=" ->
                        !Objects.equals(getValueFromNode(binaryNode.getLeft(), jsonNode), getValueFromNode(binaryNode.getRight(), jsonNode));

                // for the cases below, we compare numbers only
                case "<" ->
                        getNumericValue(binaryNode.getLeft(), jsonNode) < getNumericValue(binaryNode.getRight(), jsonNode);
                case "<=" ->
                        getNumericValue(binaryNode.getLeft(), jsonNode) <= getNumericValue(binaryNode.getRight(), jsonNode);
                case ">" ->
                        getNumericValue(binaryNode.getLeft(), jsonNode) > getNumericValue(binaryNode.getRight(), jsonNode);
                case ">=" ->
                        getNumericValue(binaryNode.getLeft(), jsonNode) >= getNumericValue(binaryNode.getRight(), jsonNode);
                default -> throw new EvaluationException("Unknown operator: " + binaryNode.getOp());
            };
        } else {
            // for cases like stand-alone true or false
            return Objects.requireNonNull(getValueFromNode(node, jsonNode), "Node value is null").equalsIgnoreCase("true");
        }
    }

    private String getValueFromNode(Parser.ASTNode node, JsonNode jsonNode) {
        try {
            if (node instanceof Parser.ValueNode valueNode) {
                // fetch data from JSON
                JsonNode targetNode = jsonNode.at("/" + valueNode.getValue().replace('.', '/'));
                if (targetNode.isNull() || targetNode.isMissingNode()) {
                    return null;
                }
                return targetNode.asText();

            }
            // get the string value
            else if (node instanceof Parser.StringNode) {
                return ((Parser.StringNode) node).getValue();
            }
            // get number value as string
            else if (node instanceof Parser.NumberNode) {
                return String.valueOf(((Parser.NumberNode) node).getValue());
            }
            // get null
            else if (node instanceof Parser.NullNode) {
                return null;
            }
            // get boolean value as string
            else if (node instanceof Parser.BooleanNode) {
                return String.valueOf(((Parser.BooleanNode) node).getValue());
            }
            throw new EvaluationException("Unknown node type for fetching value: " + node.getClass());
        } catch (Exception e) {
            throw new EvaluationException("Error fetching value from node", e);
        }
    }

    private double getNumericValue(Parser.ASTNode node, JsonNode jsonNode) {
        try {
            // get the value like before but transform it to a number
            String value = getValueFromNode(node, jsonNode);
            if (value == null) {
                throw new EvaluationException("Numeric value is null");
            }
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new EvaluationException("Failed to convert string to number", e);
        }
    }
}

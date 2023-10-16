package com.example.expressioneval.controller;

import com.example.expressioneval.analysis.Parser;
import com.example.expressioneval.model.LogicalExpression;
import com.example.expressioneval.service.LogicalExpressionService;
import com.example.expressioneval.service.SerializationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * This controller provides endpoints for storing and evaluating logical expressions.
 */
@RestController
public class ExpressionController {

    private final LogicalExpressionService logicalExpressionService;

    private final SerializationService serializationService;

    public ExpressionController(LogicalExpressionService logicalExpressionService, SerializationService serializationService) {
        this.logicalExpressionService = logicalExpressionService;
        this.serializationService = serializationService;
    }

    /**
     * Endpoint to store a logical expression with a given name.
     * The stored expression is assigned a unique identifier (ID) which is returned.
     *
     * @param name The name of the logical expression.
     * @param value The actual logical condition.
     * @return A map containing the unique ID of the saved expression and its value.
     */
    @PostMapping("/expression")
    public ResponseEntity<Map<String, Object>> saveExpression(@RequestParam String name, @RequestParam String value) {
        String processedInput = logicalExpressionService.preProcessInput(value);
        Parser.ASTNode node = logicalExpressionService.parseExpression(logicalExpressionService.tokenizeExpression(processedInput));
        String serializedAST = serializationService.serialize(node);
        LogicalExpression logicalExpression = logicalExpressionService.save(name, processedInput, serializedAST);

        Map<String, Object> response = new HashMap<>();
        response.put("id", logicalExpression.getId());
        response.put("value", logicalExpression.getExpressionValue());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint to evaluate a previously stored logical expression against provided JSON data.
     * The unique ID is used to fetch the stored expression.
     *
     * @param id The unique identifier (ID) of the logical expression.
     * @param jsonData The JSON data against which the expression is to be evaluated.
     * @return A map containing the result of the evaluation (true or false).
     */
    @PostMapping("/evaluate")
    public Map<String, Object> evaluateExpression(@RequestParam Long id, @RequestBody String jsonData) {
        LogicalExpression expr = logicalExpressionService.getById(id);
        Parser.ASTNode node = serializationService.deserialize(expr.getAstJson());
        boolean result = logicalExpressionService.evaluateExpression(node, jsonData);

        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("expr", expr.getExpressionValue());
        return response;
    }
}

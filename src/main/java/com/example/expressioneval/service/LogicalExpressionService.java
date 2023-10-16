package com.example.expressioneval.service;

import com.example.expressioneval.analysis.Evaluator;
import com.example.expressioneval.analysis.Parser;
import com.example.expressioneval.analysis.Tokenizer;
import com.example.expressioneval.exception.exceptions.ExpressionNotFoundException;
import com.example.expressioneval.exception.exceptions.InputProcessingException;
import com.example.expressioneval.model.LogicalExpression;
import com.example.expressioneval.repository.LogicalExpressionRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class LogicalExpressionService {

    private final LogicalExpressionRepository repository;

    private final Tokenizer tokenizer;

    private final ApplicationContext context;

    private final Evaluator evaluator;

    public LogicalExpressionService(LogicalExpressionRepository repository, Tokenizer tokenizer, Evaluator evaluator,
                                    ApplicationContext context) {
        this.repository = repository;
        this.tokenizer = tokenizer;
        this.context = context;
        this.evaluator = evaluator;
    }

    @Transactional(readOnly = true)
    public LogicalExpression getById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ExpressionNotFoundException("Expression not found with ID: " + id));
    }

    public LogicalExpression save(String name, String expression, String serializedAST) {
        LogicalExpression expr = new LogicalExpression();
        expr.setName(name);
        expr.setExpressionValue(expression);
        expr.setAstJson(serializedAST);
        return repository.save(expr);
    }

    public String preProcessInput(String expression) {
        String toReturn = expression.replaceAll("&&", "AND").replaceAll("\\|\\|", "OR");
        if (toReturn.length() > 2000) {
            throw new InputProcessingException("Input string exceeds the maximum length.");
        }
        return toReturn;
    }

    public ArrayList<Tokenizer.Token> tokenizeExpression(String expression) {
        return tokenizer.tokenize(expression);
    }

    public Parser.ASTNode parseExpression(ArrayList<Tokenizer.Token> tokens) {
        Parser parser = context.getBean(Parser.class);
        return parser.parse(tokens);
    }
    public boolean evaluateExpression(Parser.ASTNode tree, String jsonData) {
        return evaluator.evaluate(tree, jsonData);
    }
}

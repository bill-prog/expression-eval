package com.example.expressioneval.controller;

import com.example.expressioneval.analysis.Parser;
import com.example.expressioneval.model.LogicalExpression;
import com.example.expressioneval.repository.LogicalExpressionRepository;
import com.example.expressioneval.service.LogicalExpressionService;
import com.example.expressioneval.service.SerializationService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ExpressionControllerTestEvaluate {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LogicalExpressionRepository repository;

    @Autowired
    private LogicalExpressionService service;

    @Autowired
    private SerializationService serializationService;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    public void clearDatabase() {
        repository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private void performRequestAndAssert(String jsonData, boolean expectedResult, String name, String expression) throws Exception {
        String processedInput = service.preProcessInput(expression);
        Parser.ASTNode node = service.parseExpression(service.tokenizeExpression(processedInput));
        String jsonAst = serializationService.serialize(node);
        LogicalExpression logicalExpression = service.save(name, processedInput, jsonAst );

        mockMvc.perform(MockMvcRequestBuilders.post("/evaluate")
                        .param("id", String.valueOf(logicalExpression.getId()))
                        .content(jsonData)
                        .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value(expectedResult));
    }

    @Test
    public void evaluateAgeGreaterThan25_returnsTrue() throws Exception {
        String name = "some expression";
        String expression = "customer.age > 25";
        performRequestAndAssert("{\"customer\": {\"age\": 30}}", true, name, expression);
    }

    @Test
    public void evaluateAgeLessThan25_returnsFalse() throws Exception {
        String name = "some expression";
        String expression = "customer.age > 25";
        performRequestAndAssert("{\"customer\": {\"age\": 20}}", false, name, expression);
    }

    @Test
    public void evaluateCityEqualsSeattle_returnsTrue() throws Exception {
        String name = "some expression";
        String expression = "customer.address.city == \"Seattle\"";
        performRequestAndAssert("{\"customer\": {\"address\": {\"city\": \"Seattle\"}}}", true, name, expression);
    }

    @Test
    public void evaluateAgeGreaterThan25AndCityIsSeattle_returnsTrue() throws Exception {
        String name = "some expression";
        String expression = "(customer.age > 25) AND (customer.address.city == \"Seattle\")";
        performRequestAndAssert("{\"customer\": {\"age\": 28, \"address\": {\"city\": \"Seattle\"}}}", true, name, expression);
    }

    @Test
    public void evaluateAgeLessThan20_returnsFalse() throws Exception {
        String name = "some expression";
        String expression = "(customer.age < 20)";
        performRequestAndAssert("{\"customer\": {\"age\": 25}}", false, name, expression);
    }

    @Test
    public void evaluateAgeLessThan20OrNameIsJohn_returnsTrue() throws Exception {
        String name = "some expression";
        String expression = "(customer.age < 20) OR (customer.name == \"John\")";
        performRequestAndAssert("{\"customer\": {\"age\": 30, \"name\": \"John\"}}", true, name, expression);
    }

    @Test
    public void evaluateAddressNotNull_returnsFalse() throws Exception {
        String name = "some expression";
        String expression = "customer.address != null";
        performRequestAndAssert("{\"customer\": {}}", false, name, expression);
    }

    @Test
    public void evaluateComplexConditionsWithJohnAndAge_returnsTrue() throws Exception {
        String name = "some expression";
        String expression = "(customer.age > 25 AND customer.name == \"John\") OR (customer.address.city == \"Seattle\")";
        performRequestAndAssert("{\"customer\": {\"age\": 30, \"name\": \"John\", \"address\": {\"city\": \"New York\"}}}", true, name, expression);
    }

    @Test
    public void evaluateSalaryEquals5000OrExpensesGreaterThan1000_returnsTrue() throws Exception {
        String name = "some expression";
        String expression = "customer.salary == 5000 OR customer.expenses > 1000";
        performRequestAndAssert("{\"customer\": {\"salary\": 5000, \"expenses\": 3800}}", true, name, expression);
    }

    @Test
    public void evaluateNestedConditionsWithNameAgeAndType_returnsTrue() throws Exception {
        String name = "some expression";
        String expression = "((customer.age > 25 AND (customer.name == \"John\" OR customer.name == \"Jane\")) OR customer.type == \"VIP\") AND customer.isBlocked";
        performRequestAndAssert("{\"customer\": {\"age\": 30, \"name\": \"John\", \"type\": \"Regular\", \"isBlocked\": true}}", true, name, expression);
    }

    @Test
    public void evaluateBooleanOperationsWithNesting_returnsTrue() throws Exception {
        String name = "some expression";
        String expression = "true AND true OR (false == false)";
        performRequestAndAssert("{}", true, name, expression);
    }

    @Test
    public void evaluateBooleanComparisons_returnsFalse() throws Exception {
        String name = "some expression";
        String expression = "false != false OR true == false";
        performRequestAndAssert("{}", false, name, expression);
    }

}

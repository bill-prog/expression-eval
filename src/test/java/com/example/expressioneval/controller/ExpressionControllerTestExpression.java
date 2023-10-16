package com.example.expressioneval.controller;

import com.example.expressioneval.repository.LogicalExpressionRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ExpressionControllerTestExpression {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LogicalExpressionRepository repository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    public void clearDatabase() {
        repository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private void performRequestAndAssert(String name, String value, HttpStatus expectedResult) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/expression")
                        .param("name", name)
                        .param("value", value))
                .andExpect(MockMvcResultMatchers.status().is(expectedResult.value()));
    }


    @Test
    public void createExpressionWithAgeComparison_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "customer.age > 25";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithInvalidOperator_returnsBadRequest() throws Exception {
        String name = "some invalid expression";
        String value = "customer.age E < 25";
        performRequestAndAssert(name, value, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createExpressionWithCityComparison_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "customer.address.city == \"Seattle\"";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithAgeAndCityComparison_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "(customer.age > 25) AND (customer.address.city == \"Seattle\")";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithAgeLessThan20_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "(customer.age < 20)";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithAgeOrNameComparison_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "(customer.age < 20) OR (customer.name == \"John\")";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithNonNullAddressCheck_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "customer.address != null";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithComplexConditions_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "(customer.age > 25 AND customer.name == \"John\") OR (customer.address.city == \"Seattle\")";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithSalaryOrExpensesComparison_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "customer.salary == 5000 OR customer.expenses > 1000";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithNestedConditions_returnsCreated() throws Exception {
        String name = "some expression";
        String value = "((customer.age > 25 AND (customer.name == \"John\" OR customer.name == \"Jane\")) OR customer.type == \"VIP\") AND customer.isBlocked";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }

    @Test
    public void createExpressionWithInvalidGreaterEqualsOperator_returnsBadRequest() throws Exception {
        String name = "some expression";
        String value = "((customer.age >== 25 AND (customer.name == \"John\" OR customer.name == \"Jane\")) OR customer.type == \"VIP\") AND customer.isBlocked";
        performRequestAndAssert(name, value, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createExpressionWithInvalidAndOperator_returnsBadRequest() throws Exception {
        String name = "some expression";
        String value = "customer.age == 10 ANDD customer.job == \"Programmer\"";
        performRequestAndAssert(name, value, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createExpressionWithUnmatchedParenthesis_returnsBadRequest() throws Exception {
        String name = "some expression";
        String value = "customer.age == 10 AND customer.job == \"Programmer\")";
        performRequestAndAssert(name, value, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createExpressionWithInvalidNumberAndBooleanCombination_returnsBadRequest() throws Exception {
        String name = "some bad expression";
        String value = "5 AND (true == true)";
        performRequestAndAssert(name, value, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createExpressionWithSingleString_returnsBadRequest() throws Exception {
        String name = "some bad expression";
        String value = "\"john\"";
        performRequestAndAssert(name, value, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createExpressionWithSingleNumber_returnsBadRequest() throws Exception {
        String name = "some bad expression";
        String value = "15";
        performRequestAndAssert(name, value, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createExpressionWithNullValue_returnsBadRequest() throws Exception {
        String name = "some bad expression";
        String value = "null";
        performRequestAndAssert(name, value, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createExpressionWithNullValueEquals_returnsCreated() throws Exception {
        String name = "some bad expression";
        String value = "null != null AND null == null";
        performRequestAndAssert(name, value, HttpStatus.CREATED);
    }
}

package com.example.expressioneval.model;


import jakarta.persistence.*;

@Entity
public class LogicalExpression {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;

    @Column(length = 2000)
    private String expressionValue;

    @Lob
    private String astJson;

    public LogicalExpression() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpressionValue() {
        return expressionValue;
    }

    public void setExpressionValue(String value) {
        this.expressionValue = value;
    }

    public String getAstJson() {
        return astJson;
    }

    public void setAstJson(String astJson) {
        this.astJson = astJson;
    }
}

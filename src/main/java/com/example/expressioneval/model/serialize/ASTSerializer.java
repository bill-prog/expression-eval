package com.example.expressioneval.model.serialize;

import com.example.expressioneval.analysis.Parser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ASTSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serializeAST(Parser.ASTNode astNode) throws Exception {
        return objectMapper.writeValueAsString(astNode);
    }

    public static Parser.ASTNode deserializeAST(String json) throws Exception {
        return objectMapper.readValue(json, Parser.ASTNode.class);
    }
}

package com.example.expressioneval.service;

import com.example.expressioneval.analysis.Parser;
import com.example.expressioneval.exception.exceptions.SerializationException;
import com.example.expressioneval.model.serialize.ASTSerializer;
import org.springframework.stereotype.Service;

@Service
public class SerializationService {

    public String serialize(Parser.ASTNode node) {
        try {
            return ASTSerializer.serializeAST(node);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage());
        }
    }

    public Parser.ASTNode deserialize(String astJson) {
        try {
            return ASTSerializer.deserializeAST(astJson);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage());
        }
    }
}

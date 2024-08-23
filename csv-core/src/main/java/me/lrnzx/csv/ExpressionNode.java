package me.lrnzx.csv;

import java.util.Map;

public interface ExpressionNode {

    boolean evaluate(Map<String, String> row);
    <T> T accept(ExpressionNodeVisitor<T> visitor, Map<String, String> row);
}
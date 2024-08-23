package me.lrnzx.csv;

import java.util.Map;

public interface ExpressionNodeVisitor<T> {
    T visit(ComparisonNode node, Map<String, String> row);
    T visit(CompositeNode node, Map<String, String> row);
}
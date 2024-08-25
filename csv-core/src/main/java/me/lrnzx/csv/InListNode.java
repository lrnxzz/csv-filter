package me.lrnzx.csv;

import me.lrnzx.csv.ExpressionNode;

import java.util.List;
import java.util.Map;

public class InListNode implements ExpressionNode {
    private final String field;
    private final List<String> values;

    public InListNode(String field, List<String> values) {
        this.field = field;
        this.values = values;
    }

    @Override
    public boolean evaluate(Map<String, String> row) {
        String value = row.get(field);
        return value != null && values.contains(value);
    }

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor, Map<String, String> row) {
        return visitor.visit(this, row);
    }

    // Getters
    public String getField() { return field; }
    public List<String> getValues() { return values; }
}
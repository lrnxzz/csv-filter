package me.lrnzx.csv;

import java.util.Map;

public class ComparisonNode implements ExpressionNode {
    private final String field;
    private final String value;

    private final ComparisonOperator operator;

    public ComparisonNode(String field, ComparisonOperator operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public boolean evaluate(Map<String, String> row) {
        return new ExpressionNodeEvaluator().visit(this, row);
    }

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor, Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }
}

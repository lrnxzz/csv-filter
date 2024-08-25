package me.lrnzx.csv;

import java.util.Map;

public class BetweenNode implements ExpressionNode {
    private final String field;
    private final String lowerBound;
    private final String upperBound;

    public BetweenNode(String field, String lowerBound, String upperBound) {
        this.field = field;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public boolean evaluate(Map<String, String> row) {
        final String value = row.get(field);
        if (value == null) return false;
        return value.compareTo(lowerBound) >= 0 && value.compareTo(upperBound) <= 0;
    }

    @Override
    public <T> T accept(ExpressionNodeVisitor<T> visitor,
                        Map<String, String> row) {
        return visitor.visit(this, row);
    }

    public String getField() {
        return this.field;
    }

    public String getLowerBound() {
        return lowerBound;
    }

    public String getUpperBound() {
        return upperBound;
    }
}
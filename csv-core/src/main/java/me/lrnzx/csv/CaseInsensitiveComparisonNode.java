package me.lrnzx.csv;

import java.util.Map;

public class CaseInsensitiveComparisonNode extends ComparisonNode {
    public CaseInsensitiveComparisonNode(String field, ComparisonOperator operator, String value) {
        super(field, operator, value);
    }

    @Override
    public boolean evaluate(Map<String, String> row) {
        String fieldValue = row.get(getField());
        if (fieldValue == null) return false;

        switch (getOperator()) {
            case EQUALS:
                return fieldValue.equalsIgnoreCase(getValue());
            case NOT_EQUALS:
                return !fieldValue.equalsIgnoreCase(getValue());
            case CONTAINS:
                return fieldValue.toLowerCase().contains(getValue().toLowerCase());
            case STARTS_WITH:
                return fieldValue.toLowerCase().startsWith(getValue().toLowerCase());
            case ENDS_WITH:
                return fieldValue.toLowerCase().endsWith(getValue().toLowerCase());
            default:
                return super.evaluate(row);
        }
    }
}
package me.lrnzx.csv;

import java.util.Map;

public class ExpressionNodeEvaluator implements ExpressionNodeVisitor<Boolean> {

    @Override
    public Boolean visit(ComparisonNode node, Map<String, String> row) {
        final String fieldValue = row.get(node.getField());
        if (fieldValue == null) return false;

        switch (node.getOperator()) {
            case EQUALS: return fieldValue.equals(node.getValue());
            case NOT_EQUALS: return !fieldValue.equals(node.getValue());
            case GREATER_THAN: return fieldValue.compareTo(node.getValue()) > 0;
            case LESS_THAN: return fieldValue.compareTo(node.getValue()) < 0;
            case GREATER_THAN_OR_EQUAL: return fieldValue.compareTo(node.getValue()) >= 0;
            case LESS_THAN_OR_EQUAL: return fieldValue.compareTo(node.getValue()) <= 0;
            case CONTAINS: return fieldValue.contains(node.getValue());
            case STARTS_WITH: return fieldValue.startsWith(node.getValue());
            case ENDS_WITH: return fieldValue.endsWith(node.getValue());
            default: throw new IllegalArgumentException("Unknown operator: " + node.getOperator());
        }
    }

    @Override
    public Boolean visit(CompositeNode node, Map<String, String> row) {
        boolean result = node.getOperator() == CompositeOperator.AND;
        for (ExpressionNode child : node.getChildren()) {
            boolean childResult = child.accept(this, row);
            if (node.getOperator() == CompositeOperator.AND) {
                result &= childResult;
                if (!result) break;
            } else {
                result |= childResult;
                if (result) break;
            }
        }
        return result;
    }
}
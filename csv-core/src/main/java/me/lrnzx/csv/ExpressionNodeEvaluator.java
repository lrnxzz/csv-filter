package me.lrnzx.csv;

import java.util.Map;

/**
 * Implements the ExpressionNodeVisitor interface to provide evaluation logic
 * for both ComparisonNode and CompositeNode. This class is responsible for
 * the actual evaluation of conditions against CSV row data.
 */
public class ExpressionNodeEvaluator implements ExpressionNodeVisitor<Boolean> {

    /**
     * Evaluates a ComparisonNode against a given CSV row.
     *
     * @param node The ComparisonNode to evaluate.
     * @param row The CSV row data to evaluate against.
     * @return true if the comparison evaluates to true, false otherwise.
     */
    @Override
    public Boolean visit(final ComparisonNode node, final Map<String, String> row) {
        final String fieldValue = row.get(node.getField());
        if (fieldValue == null) return false;

        return node.getOperator().apply(fieldValue, node.getValue());
    }

    /**
     * Evaluates a CompositeNode against a given CSV row.
     * This method recursively evaluates all child nodes and combines their results
     * according to the composite node's operator (AND or OR).
     *
     * @param node The CompositeNode to evaluate.
     * @param row The CSV row data to evaluate against.
     * @return true if the composite condition evaluates to true, false otherwise.
     */
    @Override
    public Boolean visit(final CompositeNode node,
                         final Map<String, String> row) {
        boolean result = node.getOperator() == CompositeOperator.AND;
        for (ExpressionNode child : node.getChildren()) {
            final boolean childResult = child
                    .accept(this, row);
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

    @Override
    public Boolean visit(ConstantNode node, Map<String, String> row) {
        return node.getValue();
    }

    @Override
    public Boolean visit(BetweenNode node, Map<String, String> row) {
        return node.evaluate(row);
    }

    @Override
    public Boolean visit(InListNode node, Map<String, String> row) {
        return node.evaluate(row);
    }

    @Override
    public Boolean visit(DateBetweenNode node, Map<String, String> row) {
        return node.evaluate(row);
    }

    @Override
    public Boolean visit(CaseInsensitiveComparisonNode node, Map<String, String> row) {
        return node.evaluate(row);
    }

    @Override
    public Boolean visit(NotNode node, Map<String, String> row) {
        return !node.getChildNode().accept(this, row);
    }
}
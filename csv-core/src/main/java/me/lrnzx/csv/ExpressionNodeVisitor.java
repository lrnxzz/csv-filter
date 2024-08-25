package me.lrnzx.csv;

import java.util.Map;

/**
 * Defines the Visitor interface for the expression tree nodes.
 * This interface is part of the Visitor pattern implementation,
 * allowing for operations to be performed on ExpressionNode objects
 * without modifying their classes.
 *
 * @param <T> The return type of the visit methods, allowing for
 *            different types of operations to be performed on the nodes.
 */
public interface ExpressionNodeVisitor<T> {
    /**
     * Visits a ComparisonNode and performs an operation on it.
     *
     * @param node The ComparisonNode to visit.
     * @param row The CSV row data that may be needed for the operation.
     * @return The result of the operation, of type T.
     */
    T visit(final ComparisonNode node,
            final Map<String, String> row);

    /**
     * Visits a CompositeNode and performs an operation on it.
     *
     * @param node The CompositeNode to visit.
     * @param row The CSV row data that may be needed for the operation.
     * @return The result of the operation, of type T.
     */
    T visit(final CompositeNode node,
            final Map<String, String> row);

    T visit(ConstantNode node, Map<String, String> row);

    T visit(BetweenNode node, Map<String, String> row);
    T visit(InListNode node, Map<String, String> row);
    T visit(DateBetweenNode node, Map<String, String> row);
    T visit(CaseInsensitiveComparisonNode node, Map<String, String> row);
    T visit(NotNode node, Map<String, String> row);
}
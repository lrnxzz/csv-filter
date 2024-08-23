package me.lrnzx.csv;

import java.util.Map;

/**
 * Defines the contract for all nodes in the expression tree.
 * This interface is implemented by both ComparisonNode and CompositeNode,
 * allowing them to be treated polymorphically in the expression tree.
 */
public interface ExpressionNode {

    /**
     * Evaluates this node against a given CSV row.
     *
     * @param row A map representing a single row of the CSV, where keys are column names and values are cell values.
     * @return true if the condition represented by this node evaluates to true, false otherwise.
     */
    boolean evaluate(final Map<String, String> row);

    /**
     * Accepts a visitor to this node, implementing the Visitor pattern.
     * This allows for different operations to be performed on the node without changing its structure.
     *
     * @param visitor The visitor that will operate on this node.
     * @param row The CSV row data for the visitor to use.
     * @return The result of the visitor's operation on this node.
     */
    <T> T accept(final ExpressionNodeVisitor<T> visitor,
                 final Map<String, String> row);
}
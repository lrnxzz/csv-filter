package me.lrnzx.csv.fluent;

import me.lrnzx.csv.*;
import java.util.*;

/**
 * Optimizes the expression tree of a CsvFluentFilter.
 * This optimizer applies various strategies to improve filter performance.
 */
public class QueryOptimizer {

    /**
     * Optimizes the given expression node.
     *
     * @param node The root node of the expression tree to optimize.
     * @return An optimized version of the expression tree.
     */
    public ExpressionNode optimize(final ExpressionNode node) {
        return mergeCompositeNodes(
                reorderConditions(
                        simplifyConstants(node)
                )
        );
    }

    /**
     * Simplifies constant expressions in the tree.
     *
     * @param node The node to simplify.
     * @return A simplified version of the node.
     */
    private ExpressionNode simplifyConstants(final ExpressionNode node) {
        if (node instanceof NotNode) {
            NotNode notNode = (NotNode) node;
            ExpressionNode simplifiedChild = simplifyConstants(notNode.getChildNode());
            if (simplifiedChild instanceof ConstantNode) {
                return new ConstantNode(!((ConstantNode) simplifiedChild).getValue());
            }
            return new NotNode(simplifiedChild);
        }

        if (node instanceof CompositeNode) {
            final CompositeNode compositeNode = (CompositeNode) node;
            final List<ExpressionNode> optimizedChildren = new ArrayList<>();
            boolean allTrue = true;
            boolean allFalse = true;

            for (final ExpressionNode child : compositeNode.getChildren()) {
                final ExpressionNode optimizedChild = simplifyConstants(child);
                if (optimizedChild instanceof ConstantNode) {
                    final boolean value = ((ConstantNode) optimizedChild).getValue();
                    if (compositeNode.getOperator() == CompositeOperator.OR && value) {
                        return ConstantNode.TRUE;
                    }
                    if (compositeNode.getOperator() == CompositeOperator.AND && !value) {
                        return ConstantNode.FALSE;
                    }
                    allTrue = allTrue && value;
                    allFalse = allFalse && !value;
                } else {
                    optimizedChildren.add(optimizedChild);
                    allTrue = allFalse = false;
                }
            }

            if (allTrue) return ConstantNode.TRUE;
            if (allFalse) return ConstantNode.FALSE;
            if (optimizedChildren.isEmpty()) {
                return compositeNode.getOperator() == CompositeOperator.AND ? ConstantNode.TRUE : ConstantNode.FALSE;
            }
            if (optimizedChildren.size() == 1) return optimizedChildren.get(0);

            return new CompositeNode(optimizedChildren, compositeNode.getOperator());
        }
        return node;
    }

    /**
     * Reorders conditions to improve performance.
     *
     * @param node The node to reorder.
     * @return A reordered version of the node.
     */
    private ExpressionNode reorderConditions(final ExpressionNode node) {
        if (node instanceof CompositeNode) {
            final CompositeNode compositeNode = (CompositeNode) node;
            final List<ExpressionNode> reorderedChildren = new ArrayList<>(compositeNode.getChildren());
            reorderedChildren.sort(Comparator.comparingInt(this::estimateCost));
            return new CompositeNode(reorderedChildren, compositeNode.getOperator());
        }
        return node;
    }

    /**
     * Merges nested composite nodes of the same type.
     *
     * @param node The node to merge.
     * @return A merged version of the node.
     */
    private ExpressionNode mergeCompositeNodes(final ExpressionNode node) {
        if (node instanceof CompositeNode) {
            final CompositeNode compositeNode = (CompositeNode) node;
            final List<ExpressionNode> mergedChildren = new ArrayList<>();

            for (final ExpressionNode child : compositeNode.getChildren()) {
                final ExpressionNode mergedChild = mergeCompositeNodes(child);
                if (mergedChild instanceof CompositeNode &&
                        ((CompositeNode) mergedChild).getOperator() == compositeNode.getOperator()) {
                    mergedChildren.addAll(((CompositeNode) mergedChild).getChildren());
                } else {
                    mergedChildren.add(mergedChild);
                }
            }

            if (mergedChildren.size() == 1) {
                return mergedChildren.get(0);
            }
            return new CompositeNode(mergedChildren, compositeNode.getOperator());
        }
        return node;
    }

    /**
     * Estimates the computational cost of evaluating a node.
     *
     * @param node The node to estimate.
     * @return An integer representing the estimated cost.
     */
    public int estimateCost(final ExpressionNode node) {
        if (node instanceof ComparisonNode) {
            final ComparisonNode comparisonNode = (ComparisonNode) node;
            switch (comparisonNode.getOperator()) {
                case EQUALS:
                case NOT_EQUALS:
                    return 1;
                case CONTAINS:
                case STARTS_WITH:
                case ENDS_WITH:
                    return 5;
                default:
                    return 2;
            }
        } else if (node instanceof CompositeNode) {
            final CompositeNode compositeNode = (CompositeNode) node;
            return compositeNode.getChildren().stream()
                    .mapToInt(this::estimateCost)
                    .sum();
        }
        return 10;
    }
}

package me.lrnzx.csv.fluent;

import me.lrnzx.csv.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Optimizes the expression tree of a CsvFluentFilter.
 * This optimizer applies various strategies to improve filter performance,
 * including algebraic simplifications, elimination of redundant conditions,
 * combination of similar conditions, simplification of ranges, and more.
 */
public class QueryOptimizer {

    /**
     * Optimizes the given expression node.
     *
     * @param node The root node of the expression tree to optimize.
     * @return An optimized version of the expression tree.
     */
    public ExpressionNode optimize(final ExpressionNode node) {
        return Stream.<Function<ExpressionNode, ExpressionNode>>of(
                this::applyAlgebraicSimplifications,
                this::simplifyConstants,
                this::eliminateRedundantConditions,
                this::combineSimilarConditions,
                this::simplifyRanges,
                this::applyRewriteRules,
                this::mergeCompositeNodes,
                this::reorderConditions
        ).reduce(Function.identity(),
                Function::andThen
        ).apply(node);
    }

    /**
     * Applies algebraic simplifications to the expression tree,
     * such as De Morgan's laws and elimination of double negations.
     *
     * @param node The expression node to simplify.
     * @return A simplified expression node.
     */
    private ExpressionNode applyAlgebraicSimplifications(ExpressionNode node) {
        if (node instanceof NotNode) {
            ExpressionNode child = ((NotNode) node).getChildNode();

            // Eliminate double negation: NOT(NOT A) => A
            if (child instanceof NotNode) {
                return applyAlgebraicSimplifications(((NotNode) child).getChildNode());
            }

            // Apply De Morgan's laws
            if (child instanceof CompositeNode) {
                CompositeNode compositeChild = (CompositeNode) child;
                CompositeOperator newOperator = compositeChild.getOperator().opposite();
                List<ExpressionNode> negatedChildren = compositeChild.getChildren().stream()
                        .map(grandChild -> new NotNode(grandChild))
                        .map(this::applyAlgebraicSimplifications)
                        .collect(Collectors.toList());

                return applyAlgebraicSimplifications(new CompositeNode(negatedChildren, newOperator));
            }

            return new NotNode(applyAlgebraicSimplifications(child));
        } else if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;
            List<ExpressionNode> simplifiedChildren = compositeNode.getChildren().stream()
                    .map(this::applyAlgebraicSimplifications)
                    .collect(Collectors.toList());
            return new CompositeNode(simplifiedChildren, compositeNode.getOperator());
        } else {
            return node;
        }
    }

    /**
     * Simplifies constant expressions in the tree.
     *
     * @param node The node to simplify.
     * @return A simplified version of the node.
     */
    private ExpressionNode simplifyConstants(final ExpressionNode node) {
        if (node instanceof ConstantNode) {
            return node;
        }

        if (node instanceof NotNode) {
            ExpressionNode simplifiedChild = simplifyConstants(((NotNode) node).getChildNode());
            if (simplifiedChild instanceof ConstantNode) {
                boolean value = !((ConstantNode) simplifiedChild).getValue();
                return new ConstantNode(value);
            }
            return new NotNode(simplifiedChild);
        }

        if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;
            List<ExpressionNode> optimizedChildren = compositeNode.getChildren().stream()
                    .map(this::simplifyConstants)
                    .collect(Collectors.toList());

            boolean allTrue = compositeNode.getOperator() == CompositeOperator.AND;
            boolean allFalse = compositeNode.getOperator() == CompositeOperator.OR;

            Iterator<ExpressionNode> iterator = optimizedChildren.iterator();
            while (iterator.hasNext()) {
                ExpressionNode child = iterator.next();
                if (child instanceof ConstantNode) {
                    boolean value = ((ConstantNode) child).getValue();
                    if (compositeNode.getOperator() == CompositeOperator.OR && value) {
                        return ConstantNode.TRUE;
                    }
                    if (compositeNode.getOperator() == CompositeOperator.AND && !value) {
                        return ConstantNode.FALSE;
                    }
                    allTrue &= value;
                    allFalse &= !value;
                    iterator.remove();
                } else {
                    allTrue = false;
                    allFalse = false;
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
     * Eliminates redundant conditions from the expression tree,
     * such as duplicate conditions and contradictory conditions.
     *
     * @param node The node to process.
     * @return A node with redundant conditions eliminated.
     */
    private ExpressionNode eliminateRedundantConditions(ExpressionNode node) {
        if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;

            Set<ExpressionNode> uniqueChildren = new HashSet<>();
            Set<ExpressionNode> negatedChildren = new HashSet<>();

            compositeNode.getChildren().stream()
                    .map(this::eliminateRedundantConditions)
                    .forEach(child -> {
                        if (child instanceof NotNode) {
                            negatedChildren.add(((NotNode) child).getChildNode());
                        } else {
                            uniqueChildren.add(child);
                        }
                    });

            // Check for contradictory conditions
            if (uniqueChildren.stream().anyMatch(negatedChildren::contains)) {
                return compositeNode.getOperator() == CompositeOperator.AND
                        ? ConstantNode.FALSE
                        : ConstantNode.TRUE;
            }

            // Combine unique and negated conditions
            List<ExpressionNode> newChildren = Stream.concat(
                    uniqueChildren.stream(),
                    negatedChildren.stream().map(NotNode::new)
            ).collect(Collectors.toList());

            if (newChildren.size() == 1) {
                return newChildren.get(0);
            }

            return new CompositeNode(newChildren, compositeNode.getOperator());
        } else if (node instanceof NotNode) {
            ExpressionNode simplifiedChild = eliminateRedundantConditions(((NotNode) node).getChildNode());
            return new NotNode(simplifiedChild);
        } else {
            return node;
        }
    }

    /**
     * Combines similar conditions affecting the same field into a single condition.
     *
     * @param node The node to process.
     * @return A node with similar conditions combined.
     */
    private ExpressionNode combineSimilarConditions(ExpressionNode node) {
        if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;

            Map<String, List<ComparisonNode>> fieldComparisons = compositeNode.getChildren().stream()
                    .filter(child -> child instanceof ComparisonNode)
                    .map(child -> (ComparisonNode) child)
                    .collect(Collectors.groupingBy(ComparisonNode::getField));

            List<ExpressionNode> otherChildren = compositeNode.getChildren().stream()
                    .filter(child -> !(child instanceof ComparisonNode))
                    .map(this::combineSimilarConditions)
                    .collect(Collectors.toList());

            List<ExpressionNode> newChildren = Stream.concat(
                    fieldComparisons.entrySet().stream()
                            .map(entry -> combineFieldComparisons(entry.getKey(), entry.getValue(), compositeNode.getOperator())),
                    otherChildren.stream()
            ).collect(Collectors.toList());

            if (newChildren.size() == 1) {
                return newChildren.get(0);
            }

            return new CompositeNode(newChildren, compositeNode.getOperator());
        } else {
            return node;
        }
    }

    /**
     * Combines multiple comparisons on the same field into a single condition.
     *
     * @param field       The field name.
     * @param comparisons The list of comparisons to combine.
     * @param operator    The composite operator (AND/OR).
     * @return A combined expression node.
     */
    private ExpressionNode combineFieldComparisons(String field, List<ComparisonNode> comparisons, CompositeOperator operator) {
        if (comparisons.size() == 1) {
            return comparisons.get(0);
        }

        // Group comparisons by operator
        Map<ComparisonOperator, List<ComparisonNode>> comparisonsByOperator = comparisons.stream()
                .collect(Collectors.groupingBy(ComparisonNode::getOperator));

        // Handle equality comparisons
        List<ComparisonNode> equalityComparisons = comparisonsByOperator.getOrDefault(ComparisonOperator.EQUALS, Collections.emptyList());
        if (!equalityComparisons.isEmpty()) {
            if (operator == CompositeOperator.AND) {
                // In AND, multiple equality comparisons on the same field with different values are contradictory
                Set<String> values = equalityComparisons.stream()
                        .map(ComparisonNode::getValue)
                        .collect(Collectors.toSet());
                if (values.size() == 1) {
                    return new ComparisonNode(field, ComparisonOperator.EQUALS, values.iterator().next());
                } else {
                    return ConstantNode.FALSE;
                }
            } else {
                // In OR, combine equality comparisons into an IN list
                List<String> values = equalityComparisons.stream()
                        .map(ComparisonNode::getValue)
                        .collect(Collectors.toList());
                return new InListNode(field, values);
            }
        }

        // Handle range comparisons
        Range combinedRange = comparisons.stream()
                .filter(comp -> comp.getOperator().isRangeOperator())
                .map(Range::fromComparisonNode)
                .reduce((r1, r2) -> operator == CompositeOperator.AND ? r1.intersect(r2) : r1.union(r2))
                .orElse(null);

        if (combinedRange == null || combinedRange.isEmpty()) {
            return operator == CompositeOperator.AND ? ConstantNode.FALSE : ConstantNode.TRUE;
        }

        ExpressionNode rangeNode = combinedRange.toExpressionNode(field);

        // Handle other comparisons (e.g., CONTAINS, STARTS_WITH)
        List<ExpressionNode> otherComparisons = comparisonsByOperator.entrySet().stream()
                .filter(e -> !e.getKey().isRangeOperator() && e.getKey() != ComparisonOperator.EQUALS)
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());

        List<ExpressionNode> nodes = Stream.concat(
                Stream.of(rangeNode),
                otherComparisons.stream()
        ).collect(Collectors.toList());

        if (nodes.size() == 1) {
            return nodes.get(0);
        }

        return new CompositeNode(nodes, operator);
    }

    /**
     * Simplifies range conditions by merging overlapping or adjacent ranges.
     *
     * @param node The node to simplify.
     * @return A node with simplified ranges.
     */
    private ExpressionNode simplifyRanges(ExpressionNode node) {
        // In this implementation, we assume that combineSimilarConditions handles range simplification
        return node;
    }

    /**
     * Applies rewrite rules to the expression tree to simplify it further.
     *
     * @param node The node to rewrite.
     * @return A rewritten expression node.
     */
    private ExpressionNode applyRewriteRules(ExpressionNode node) {
        if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;

            List<ExpressionNode> rewrittenChildren = compositeNode.getChildren().stream()
                    .map(this::applyRewriteRules)
                    .collect(Collectors.toList());

            ExpressionNode rewrittenNode = new CompositeNode(rewrittenChildren, compositeNode.getOperator());

            // Example rewrite rule: A AND (A OR B) => A
            if (compositeNode.getOperator() == CompositeOperator.AND) {
                Set<ExpressionNode> childSet = new HashSet<>(rewrittenChildren);
                for (ExpressionNode child : rewrittenChildren) {
                    if (child instanceof CompositeNode) {
                        CompositeNode childComposite = (CompositeNode) child;
                        if (childComposite.getOperator() == CompositeOperator.OR) {
                            for (ExpressionNode grandChild : childComposite.getChildren()) {
                                if (childSet.contains(grandChild)) {
                                    return grandChild;
                                }
                            }
                        }
                    }
                }
            }

            return rewrittenNode;
        } else if (node instanceof NotNode) {
            ExpressionNode rewrittenChild = applyRewriteRules(((NotNode) node).getChildNode());
            return new NotNode(rewrittenChild);
        } else {
            return node;
        }
    }

    /**
     * Merges nested composite nodes of the same type.
     *
     * @param node The node to merge.
     * @return A merged version of the node.
     */
    private ExpressionNode mergeCompositeNodes(final ExpressionNode node) {
        if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;

            List<ExpressionNode> mergedChildren = compositeNode.getChildren().stream()
                    .map(this::mergeCompositeNodes)
                    .flatMap(child -> {
                        if (child instanceof CompositeNode &&
                                ((CompositeNode) child).getOperator() == compositeNode.getOperator()) {
                            return ((CompositeNode) child).getChildren().stream();
                        } else {
                            return Stream.of(child);
                        }
                    })
                    .collect(Collectors.toList());

            if (mergedChildren.size() == 1) {
                return mergedChildren.get(0);
            }

            return new CompositeNode(mergedChildren, compositeNode.getOperator());
        } else if (node instanceof NotNode) {
            ExpressionNode mergedChild = mergeCompositeNodes(((NotNode) node).getChildNode());
            return new NotNode(mergedChild);
        } else {
            return node;
        }
    }

    /**
     * Reorders conditions to improve performance based on estimated cost.
     *
     * @param node The node to reorder.
     * @return A reordered version of the node.
     */
    private ExpressionNode reorderConditions(final ExpressionNode node) {
        if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;

            List<ExpressionNode> reorderedChildren = compositeNode.getChildren().stream()
                    .map(this::reorderConditions)
                    .sorted(Comparator.comparingInt(this::estimateCost))
                    .collect(Collectors.toList());

            return new CompositeNode(reorderedChildren, compositeNode.getOperator());
        } else if (node instanceof NotNode) {
            ExpressionNode reorderedChild = reorderConditions(((NotNode) node).getChildNode());
            return new NotNode(reorderedChild);
        } else {
            return node;
        }
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
            int baseCost;
            switch (comparisonNode.getOperator()) {
                case EQUALS:
                case NOT_EQUALS:
                    baseCost = 1;
                    break;
                case GREATER_THAN:
                case LESS_THAN:
                case GREATER_THAN_OR_EQUAL:
                case LESS_THAN_OR_EQUAL:
                    baseCost = 2;
                    break;
                case CONTAINS:
                case STARTS_WITH:
                case ENDS_WITH:
                    baseCost = 5;
                    break;
                case MATCHES:
                    baseCost = 10;
                    break;
                default:
                    baseCost = 3;
                    break;
            }
            return baseCost;
        } else if (node instanceof CompositeNode) {
            CompositeNode compositeNode = (CompositeNode) node;
            if (compositeNode.getOperator() == CompositeOperator.AND) {
                // For AND, the cost is influenced by the highest cost
                return compositeNode.getChildren().stream()
                        .mapToInt(this::estimateCost)
                        .max().orElse(0);
            } else {
                // For OR, the cost is the sum of the costs
                return compositeNode.getChildren().stream()
                        .mapToInt(this::estimateCost)
                        .sum();
            }
        } else if (node instanceof NotNode) {
            return estimateCost(((NotNode) node).getChildNode());
        } else {
            return 1;
        }
    }

    /**
     * Helper class representing a range for range comparisons.
     */
    private static class Range {
        private final Double lowerBound;
        private final boolean lowerInclusive;
        private final Double upperBound;
        private final boolean upperInclusive;

        private Range(Double lowerBound, boolean lowerInclusive, Double upperBound, boolean upperInclusive) {
            this.lowerBound = lowerBound;
            this.lowerInclusive = lowerInclusive;
            this.upperBound = upperBound;
            this.upperInclusive = upperInclusive;
        }

        /**
         * Creates a Range object from a ComparisonNode.
         *
         * @param node The ComparisonNode to convert.
         * @return A Range representing the comparison.
         */
        public static Range fromComparisonNode(ComparisonNode node) {
            double value = parseDouble(node.getValue());
            switch (node.getOperator()) {
                case GREATER_THAN:
                    return new Range(value, false, null, false);
                case GREATER_THAN_OR_EQUAL:
                    return new Range(value, true, null, false);
                case LESS_THAN:
                    return new Range(null, false, value, false);
                case LESS_THAN_OR_EQUAL:
                    return new Range(null, false, value, true);
                case EQUALS:
                    return new Range(value, true, value, true);
                default:
                    throw new IllegalArgumentException("Unsupported operator for range: " + node.getOperator());
            }
        }

        /**
         * Intersects this range with another range.
         *
         * @param other The other range to intersect with.
         * @return The intersection of the two ranges.
         */
        public Range intersect(Range other) {
            Double newLower = null;
            boolean newLowerInclusive = false;
            if (this.lowerBound == null) {
                newLower = other.lowerBound;
                newLowerInclusive = other.lowerInclusive;
            } else if (other.lowerBound == null) {
                newLower = this.lowerBound;
                newLowerInclusive = this.lowerInclusive;
            } else if (this.lowerBound > other.lowerBound || (this.lowerBound.equals(other.lowerBound) && !this.lowerInclusive && other.lowerInclusive)) {
                newLower = this.lowerBound;
                newLowerInclusive = this.lowerInclusive;
            } else {
                newLower = other.lowerBound;
                newLowerInclusive = other.lowerInclusive;
            }

            Double newUpper = null;
            boolean newUpperInclusive = false;
            if (this.upperBound == null) {
                newUpper = other.upperBound;
                newUpperInclusive = other.upperInclusive;
            } else if (other.upperBound == null) {
                newUpper = this.upperBound;
                newUpperInclusive = this.upperInclusive;
            } else if (this.upperBound < other.upperBound || (this.upperBound.equals(other.upperBound) && !this.upperInclusive && other.upperInclusive)) {
                newUpper = this.upperBound;
                newUpperInclusive = this.upperInclusive;
            } else {
                newUpper = other.upperBound;
                newUpperInclusive = other.upperInclusive;
            }

            if (newLower != null && newUpper != null) {
                if (newLower > newUpper || (newLower.equals(newUpper) && (!newLowerInclusive || !newUpperInclusive))) {
                    return empty();
                }
            }

            return new Range(newLower, newLowerInclusive, newUpper, newUpperInclusive);
        }

        /**
         * Unions this range with another range.
         *
         * @param other The other range to union with.
         * @return The union of the two ranges.
         */
        public Range union(Range other) {
            Double newLower = null;
            boolean newLowerInclusive = false;
            if (this.lowerBound == null || other.lowerBound == null) {
                newLower = null;
            } else if (this.lowerBound < other.lowerBound || (this.lowerBound.equals(other.lowerBound) && (this.lowerInclusive || other.lowerInclusive))) {
                newLower = this.lowerBound;
                newLowerInclusive = this.lowerInclusive;
            } else {
                newLower = other.lowerBound;
                newLowerInclusive = other.lowerInclusive;
            }

            Double newUpper = null;
            boolean newUpperInclusive = false;
            if (this.upperBound == null || other.upperBound == null) {
                newUpper = null;
            } else if (this.upperBound > other.upperBound || (this.upperBound.equals(other.upperBound) && (this.upperInclusive || other.upperInclusive))) {
                newUpper = this.upperBound;
                newUpperInclusive = this.upperInclusive;
            } else {
                newUpper = other.upperBound;
                newUpperInclusive = other.upperInclusive;
            }

            return new Range(newLower, newLowerInclusive, newUpper, newUpperInclusive);
        }

        /**
         * Checks if the range is empty.
         *
         * @return True if the range is empty, false otherwise.
         */
        public boolean isEmpty() {
            if (lowerBound == null || upperBound == null) {
                return false;
            }
            return lowerBound > upperBound || (lowerBound.equals(upperBound) && (!lowerInclusive || !upperInclusive));
        }

        /**
         * Converts the range into an ExpressionNode.
         *
         * @param field The field name.
         * @return An ExpressionNode representing the range.
         */
        public ExpressionNode toExpressionNode(String field) {
            if (isEmpty()) {
                return ConstantNode.FALSE;
            }
            if (lowerBound != null && upperBound != null && lowerBound.equals(upperBound) && lowerInclusive && upperInclusive) {
                return new ComparisonNode(field, ComparisonOperator.EQUALS, lowerBound.toString());
            }
            if (lowerBound != null && upperBound != null) {
                return new BetweenNode(field, lowerBound.toString(), upperBound.toString(), lowerInclusive, upperInclusive);
            }
            if (lowerBound != null) {
                ComparisonOperator op = lowerInclusive ? ComparisonOperator.GREATER_THAN_OR_EQUAL : ComparisonOperator.GREATER_THAN;
                return new ComparisonNode(field, op, lowerBound.toString());
            }
            if (upperBound != null) {
                ComparisonOperator op = upperInclusive ? ComparisonOperator.LESS_THAN_OR_EQUAL : ComparisonOperator.LESS_THAN;
                return new ComparisonNode(field, op, upperBound.toString());
            }
            return ConstantNode.TRUE;
        }

        private static Range empty() {
            return new Range(0.0, false, 0.0, false);
        }

        private static double parseDouble(String value) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse value as double: " + value, e);
            }
        }
    }
}

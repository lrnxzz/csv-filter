import me.lrnzx.csv.*;
import me.lrnzx.csv.fluent.QueryOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueryOptimizerTest {

    private QueryOptimizer optimizer;

    @BeforeEach
    public void setUp() {
        optimizer = new QueryOptimizer();
    }

    @Test
    public void testSimplifyConstantsTrue() {
        // Test simplification of (true OR true) to true
        final ExpressionNode node = createCompositeNode(
                Arrays.asList(ConstantNode.TRUE, ConstantNode.TRUE),
                CompositeOperator.OR
        );
        final ExpressionNode optimized = optimizer.optimize(node);
        assertEquals(ConstantNode.TRUE, optimized, "Optimized node should be TRUE");
    }

    @Test
    public void testSimplifyConstantsFalse() {
        // Test simplification of (false AND false) to false
        final ExpressionNode node = createCompositeNode(
                Arrays.asList(ConstantNode.FALSE, ConstantNode.FALSE),
                CompositeOperator.AND
        );
        final ExpressionNode optimized = optimizer.optimize(node);
        assertEquals(ConstantNode.FALSE, optimized, "Optimized node should be FALSE");
    }

    @Test
    public void testReorderConditions() {
        // Test reordering of conditions based on estimated cost
        final ExpressionNode node = createCompositeNode(
                Arrays.asList(
                        new ComparisonNode("field", ComparisonOperator.STARTS_WITH, "prefix"),
                        new ComparisonNode("field", ComparisonOperator.EQUALS, "value"),
                        new ComparisonNode("field", ComparisonOperator.CONTAINS, "substring")
                ),
                CompositeOperator.AND
        );
        final ExpressionNode optimized = optimizer.optimize(node);
        assertReorderedConditions(optimized);
    }

    @Test
    public void testMergeCompositeNodes() {
        final ExpressionNode node = createCompositeNode(
                Arrays.asList(
                        createCompositeNode(
                                Arrays.asList(
                                        new ComparisonNode("field1", ComparisonOperator.EQUALS, "value1"),
                                        new ComparisonNode("field2", ComparisonOperator.EQUALS, "value2")
                                ),
                                CompositeOperator.AND
                        ),
                        new ComparisonNode("field3", ComparisonOperator.EQUALS, "value3")
                ),
                CompositeOperator.AND
        );
        final ExpressionNode optimized = optimizer.optimize(node);
        assertMergedCompositeNodes(optimized);
    }

    @Test
    public void testComplexOptimization() {
        // Test complex optimization: (A AND (B OR C)) to (A AND B OR A AND C)
        final ExpressionNode node = createCompositeNode(
                Arrays.asList(
                        new ComparisonNode("field1", ComparisonOperator.EQUALS, "value1"),
                        createCompositeNode(
                                Arrays.asList(
                                        new ComparisonNode("field2", ComparisonOperator.EQUALS, "value2"),
                                        new ComparisonNode("field3", ComparisonOperator.EQUALS, "value3")
                                ),
                                CompositeOperator.OR
                        )
                ),
                CompositeOperator.AND
        );
        final ExpressionNode optimized = optimizer.optimize(node);
        assertComplexOptimization(optimized);
    }

    private void assertReorderedConditions(final ExpressionNode node) {
        assertTrue(node instanceof CompositeNode, "Optimized node should be CompositeNode");
        final CompositeNode compositeNode = (CompositeNode) node;
        assertEquals(3, compositeNode.getChildren().size(), "There should be three children after optimization");
        assertConditionOrder(compositeNode.getChildren());
    }

    private void assertMergedCompositeNodes(final ExpressionNode node) {
        assertTrue(node instanceof CompositeNode, "Optimized node should be CompositeNode");
        final CompositeNode compositeNode = (CompositeNode) node;
        assertEquals(3, compositeNode.getChildren().size(), "There should be three children after merging");
        assertTrue(compositeNode.getChildren().get(0) instanceof ComparisonNode, "First child should be ComparisonNode");
        assertTrue(compositeNode.getChildren().get(1) instanceof ComparisonNode, "Second child should be ComparisonNode");
        assertTrue(compositeNode.getChildren().get(2) instanceof ComparisonNode, "Third child should be ComparisonNode");
    }

    private void assertComplexOptimization(final ExpressionNode node) {
        assertTrue(node instanceof CompositeNode, "Optimized node should be CompositeNode");
        final CompositeNode compositeNode = (CompositeNode) node;
        assertEquals(2, compositeNode.getChildren().size(), "There should be two children after optimization");
        assertTrue(compositeNode.getChildren().get(0) instanceof CompositeNode, "First child should be CompositeNode");
        assertTrue(compositeNode.getChildren().get(1) instanceof CompositeNode, "Second child should be CompositeNode");
    }

    private void assertConditionOrder(final List<ExpressionNode> children) {
        assertEquals(1, optimizer.estimateCost(children.get(0)), "The first condition should be the cheapest");
        assertEquals(2, optimizer.estimateCost(children.get(1)), "The second condition should be the next cheapest");
        assertEquals(5, optimizer.estimateCost(children.get(2)), "The last condition should be the most expensive");
    }

    private ExpressionNode createCompositeNode(final List<ExpressionNode> children,
                                               final CompositeOperator operator) {
        return new CompositeNode(children, operator);
    }
}

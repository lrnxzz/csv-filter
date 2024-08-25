import me.lrnzx.csv.*;
import me.lrnzx.csv.fluent.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionNodeEvaluatorTest {

    @Test
    void testVisitComparisonNode() {
        final ExpressionNodeEvaluator evaluator = new ExpressionNodeEvaluator();
        final Map<String, String> row = new HashMap<>();

        row.put("name", "Lorenzo");
        row.put("age", "19");

        final ComparisonNode nameEquals = new ComparisonNode("name", ComparisonOperator.EQUALS, "Lorenzo");
        assertTrue(evaluator.visit(nameEquals, row));

        final ComparisonNode ageGreaterThan = new ComparisonNode("age", ComparisonOperator.GREATER_THAN, "18");
        assertTrue(evaluator.visit(ageGreaterThan, row));
    }

    @Test
    void testVisitCompositeNode() {
        final ExpressionNodeEvaluator evaluator = new ExpressionNodeEvaluator();

        final Map<String, String> row = new HashMap<>();

        row.put("name", "Erica");
        row.put("age", "23");

        final ComparisonNode nameEquals = new ComparisonNode("name", ComparisonOperator.EQUALS, "Erica");
        final ComparisonNode ageGreaterThan = new ComparisonNode("age", ComparisonOperator.GREATER_THAN, "22");

        final CompositeNode andNode = new CompositeNode(Arrays.asList(nameEquals, ageGreaterThan), CompositeOperator.AND);
        assertTrue(evaluator.visit(andNode, row));

        final ComparisonNode ageLessThan = new ComparisonNode("age", ComparisonOperator.LESS_THAN, "25");
        final CompositeNode orNode = new CompositeNode(Arrays.asList(nameEquals, ageLessThan), CompositeOperator.OR);
        assertTrue(evaluator.visit(orNode, row));
    }
}
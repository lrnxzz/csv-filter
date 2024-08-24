import me.lrnzx.csv.ComparisonNode;
import me.lrnzx.csv.ComparisonOperator;
import me.lrnzx.csv.CompositeNode;
import me.lrnzx.csv.CompositeOperator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompositeNodeTest {

    @Test
    void testEvaluateAndOperator() {
        Map<String, String> row = new HashMap<>();

        row.put("name", "Lorenzo Losi");
        row.put("age", "19");

        final ComparisonNode nameEquals = new ComparisonNode("name", ComparisonOperator.EQUALS, "Lorenzo Losi");
        final ComparisonNode ageGreaterThan = new ComparisonNode("age", ComparisonOperator.GREATER_THAN, "18");

        CompositeNode andNode = new CompositeNode(Arrays.asList(nameEquals, ageGreaterThan), CompositeOperator.AND);
        assertTrue(andNode.evaluate(row));
    }

    @Test
    void testEvaluateOrOperator() {
        final Map<String, String> row = new HashMap<>();

        row.put("name", "Erica Rosa");
        row.put("age", "23");

        final ComparisonNode nameEquals = new ComparisonNode("name", ComparisonOperator.EQUALS, "Erica Rosa");
        final ComparisonNode ageGreaterThan = new ComparisonNode("age", ComparisonOperator.GREATER_THAN, "22");

        final CompositeNode orNode = new CompositeNode(Arrays.asList(nameEquals, ageGreaterThan), CompositeOperator.OR);
        assertTrue(orNode.evaluate(row));
    }

    @Test
    void testNestedCompositeNode() {
        final Map<String, String> row = new HashMap<>();

        row.put("name", "Erikitcha");
        row.put("age", "23");
        row.put("city", "Caçapava");

        final ComparisonNode nameEquals = new ComparisonNode("name", ComparisonOperator.EQUALS, "Erikitcha");
        final ComparisonNode ageGreaterThan = new ComparisonNode("age", ComparisonOperator.GREATER_THAN, "22");
        final ComparisonNode cityContains = new ComparisonNode("city", ComparisonOperator.CONTAINS, "pava");

        final CompositeNode innerAnd = new CompositeNode(Arrays.asList(nameEquals, ageGreaterThan), CompositeOperator.AND);
        final CompositeNode outerOr = new CompositeNode(Arrays.asList(innerAnd, cityContains), CompositeOperator.OR);

        assertTrue(outerOr.evaluate(row));
    }

    @Test
    void testEmptyCompositeNode() {
        Map<String, String> row = new HashMap<>();

        final CompositeNode emptyAnd = new CompositeNode(Arrays.asList(), CompositeOperator.AND);
        final CompositeNode emptyOr = new CompositeNode(Arrays.asList(), CompositeOperator.OR);

        assertTrue(emptyAnd.evaluate(row));
        assertFalse(emptyOr.evaluate(row));
    }
}
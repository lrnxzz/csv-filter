import me.lrnzx.csv.ComparisonNode;
import me.lrnzx.csv.ComparisonOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class ComparisonNodeTest {

    @ParameterizedTest
    @MethodSource("provideComparisonScenarios")
    void testEvaluateWithVariousOperators(String field, ComparisonOperator operator, String value, Map<String, String> row, boolean expected) {
        final ComparisonNode node = new ComparisonNode(field, operator, value);
        assertEquals(expected, node
                .evaluate(row));
    }

    private static Stream<Arguments> provideComparisonScenarios() {
        final Map<String, String> row = new HashMap<>();

        row.put("name", "Lorenzo Losi");
        row.put("age", "19");
        row.put("city", "São José dos Campos");

        return Stream.of(
                Arguments.of("name", ComparisonOperator.EQUALS, "Lorenzo Losi", row, true),
                Arguments.of("name", ComparisonOperator.NOT_EQUALS, "Erica", row, true),
                Arguments.of("age", ComparisonOperator.GREATER_THAN, "15", row, true),
                Arguments.of("age", ComparisonOperator.LESS_THAN, "20", row, true),
                Arguments.of("age", ComparisonOperator.GREATER_THAN_OR_EQUAL, "19", row, true),
                Arguments.of("age", ComparisonOperator.LESS_THAN_OR_EQUAL, "19", row, true),
                Arguments.of("city", ComparisonOperator.CONTAINS, "dos", row, true),
                Arguments.of("city", ComparisonOperator.STARTS_WITH, "São", row, true),
                Arguments.of("city", ComparisonOperator.ENDS_WITH, "Campos", row, true)
        );
    }

    @Test
    void testEvaluateWithNullFieldValue() {
        Map<String, String> row = new HashMap<>();
        row.put("name", "John");

        ComparisonNode ageGreaterThan = new ComparisonNode("age", ComparisonOperator.GREATER_THAN, "25");
        assertFalse(ageGreaterThan.evaluate(row));
    }
}
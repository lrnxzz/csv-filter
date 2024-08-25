
import me.lrnzx.csv.fluent.CsvFluentFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CsvFluentFilterBuilderTest {

    private Map<String, String> erica;
    private Map<String, String> lorenzo;

    @BeforeEach
    public void setUp() {
        erica = new HashMap<>();
        erica.put("name", "Erica");
        erica.put("age", "19");

        lorenzo = new HashMap<>();
        lorenzo.put("name", "Lorenzo");
        lorenzo.put("age", "23");
    }

    @Test
    public void testAgeGreaterThan20() {
        final CsvFluentFilter olderThan20 = CsvFluentFilter.where("age").isGreaterThan("20");
        assertFalse(olderThan20.evaluate(erica), "Erica should not be older than 20");
        assertTrue(olderThan20.evaluate(lorenzo), "Lorenzo should be older than 20");
    }

    @Test
    public void testAgeLessThanOrEqualTo20() {
        final CsvFluentFilter youngerThanOrEqual20 = CsvFluentFilter.where("age").isLessThanOrEqualTo("20");
        assertTrue(youngerThanOrEqual20.evaluate(erica), "Erica should be 20 or younger");
        assertFalse(youngerThanOrEqual20.evaluate(lorenzo), "Lorenzo should not be 20 or younger");
    }

    @Test
    public void testNameStartsWithEAndAgeLessThan20() {
        final CsvFluentFilter startsWithEAndYoung = CsvFluentFilter.where("name").startsWith("E")
                .and(CsvFluentFilter.where("age").isLessThan("20"));
        assertTrue(startsWithEAndYoung.evaluate(erica), "Erica's name starts with E and she's younger than 20");
        assertFalse(startsWithEAndYoung.evaluate(lorenzo), "Lorenzo's name doesn't start with E and he's not younger than 20");
    }

    @Test
    public void testNameContainsOOrAgeGreaterThan21() {
        final CsvFluentFilter nameContainsOOrOlder = CsvFluentFilter.where("name").contains("o")
                .or(CsvFluentFilter.where("age").isGreaterThan("21"));
        assertFalse(nameContainsOOrOlder.evaluate(erica), "Erica's name doesn't contain 'o' and she's not older than 21");
        assertTrue(nameContainsOOrOlder.evaluate(lorenzo), "Lorenzo's name contains 'o' and he's older than 21");
    }

    @Test
    public void testComplexFilter() {
        final CsvFluentFilter complexFilter = CsvFluentFilter.where("name").endsWith("a")
                .or(CsvFluentFilter.where("age").isGreaterThanOrEqualTo("20")
                        .and(CsvFluentFilter.where("name").contains("ren")));
        assertTrue(complexFilter.evaluate(erica), "Erica's name ends with 'a'");
        assertTrue(complexFilter.evaluate(lorenzo), "Lorenzo is older than 20 and his name contains 'ren'");
    }
}
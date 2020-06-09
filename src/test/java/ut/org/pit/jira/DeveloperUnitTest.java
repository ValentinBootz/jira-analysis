package ut.org.pit.jira;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pit.jira.model.Developer;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for Developer.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeveloperUnitTest {

    @Test
    public void testSetTotalOpenEstimateWeeks() {
        Developer developer = new Developer();

        developer.setTotalEstimateSeconds(144000L);

        assertEquals("1w", developer.getEstimate());
    }

    @Test
    public void testSetTotalOpenEstimateDays() {
        Developer developer = new Developer();

        developer.setTotalEstimateSeconds(28800L);

        assertEquals("1d", developer.getEstimate());
    }

    @Test
    public void testSetTotalOpenEstimateHours() {
        Developer developer = new Developer();

        developer.setTotalEstimateSeconds(3600L);

        assertEquals("1h", developer.getEstimate());
    }

    @Test
    public void testSetTotalOpenEstimateMinutes() {
        Developer developer = new Developer();

        developer.setTotalEstimateSeconds(60L);

        assertEquals("1m", developer.getEstimate());
    }

    @Test
    public void testSetTotalOpenEstimate() {
        Developer developer = new Developer();

        developer.setTotalEstimateSeconds(176460L);

        assertEquals("1w 1d 1h 1m", developer.getEstimate());
    }
}

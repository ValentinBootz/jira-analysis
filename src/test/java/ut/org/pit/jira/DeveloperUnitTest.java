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
    public void testSetTotalOpenEstimate() {
        Developer developer = new Developer();

        developer.setTotalOpenEstimate(2887500L);

        assertEquals(developer.getTotalOpenEstimateText(), "4w 5d 10h 5m");
    }
}

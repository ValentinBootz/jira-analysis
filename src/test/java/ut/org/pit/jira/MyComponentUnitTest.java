package ut.org.pit.jira;

import org.junit.Test;
import org.pit.jira.api.MyPluginComponent;
import org.pit.jira.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}
package ut.org.pit.jira;

import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pit.jira.access.ItemType;
import org.pit.jira.access.LoggingAndAccessService;
import org.pit.jira.model.Grant;

import javax.ws.rs.WebApplicationException;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit test for LoggingAndAccessService.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggingAndAccessServiceUnitTest {

    private static final String ITEM_TYPE = ItemType.HELP.getItemType();
    private static final String NON_EXISTENT_ITEM_TYPE = "non_existent_item_type";
    private static final String USER_RID = "test@test.de";
    private static final String USERNAME = "username";

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private UserManager userManager;

    @Mock
    private UserSearchService userSearchService;

    @Mock
    private final MockApplicationUser currentUser = new MockApplicationUser(USERNAME);

    private LoggingAndAccessService service;

    @Before
    public void setup() {
        service = new LoggingAndAccessService(jiraAuthenticationContext, userManager, userSearchService);
    }

    @Test
    public void testRequestQueryAccessWithoutFilter() {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(currentUser);
        when(currentUser.getEmailAddress()).thenReturn(USER_RID);

        Grant result = service.requestQueryAccess(ITEM_TYPE, null);

        verify(userSearchService, times(1)).findUsersAllowEmptyQuery(any(), any());
        verifyZeroInteractions(userManager);
        assertTrue(result.getGranted());
    }

    @Test
    public void testRequestQueryAccessWithFilter() {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(currentUser);
        when(currentUser.getEmailAddress()).thenReturn(USER_RID);

        Grant result = service.requestQueryAccess(ITEM_TYPE, Collections.singletonList(USERNAME));

        verify(userManager, times(1)).getUserByName(USERNAME);
        verifyZeroInteractions(userSearchService);
        assertTrue(result.getGranted());
    }

    @Test(expected = WebApplicationException.class)
    public void testRequestQueryAccessWithNonExistentItemType() {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(currentUser);
        when(currentUser.getEmailAddress()).thenReturn(USER_RID);

        service.requestQueryAccess(NON_EXISTENT_ITEM_TYPE, null);
    }
}

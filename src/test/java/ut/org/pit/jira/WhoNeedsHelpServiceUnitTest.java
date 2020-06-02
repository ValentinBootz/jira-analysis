package ut.org.pit.jira;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactory;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactoryImpl;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockApplicationUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pit.jira.help.WhoNeedsHelpService;
import org.pit.jira.model.Developer;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test for WhoNeedsHelpService.
 */
@RunWith(MockitoJUnitRunner.class)
public class WhoNeedsHelpServiceUnitTest {

    private final static String AVATAR_URL = "http://www.test.com/";
    private final static String ICON_URL = "http://www.test.com/";
    private final static String NAME_1 = "test-name-1";
    private final static String NAME_2 = "test-name-2";
    private final static Long ESTIMATE_1 = 3600L;
    private final static Long ESTIMATE_2 = 28800L;
    private final static Long ESTIMATE_3 = 7200L;
    private final static String TYPE_NAME_1 = "test-type-name-1";

    @Mock
    private SearchService searchService;

    @Mock
    private AvatarService avatarService;

    @Mock
    private SearchResults searchResults;

    @Mock
    private TimeZoneManager timeZoneManager;

    @Mock
    private MockComponentWorker componentWorker;

    @Mock
    private final MockIssue issue1 = new MockIssue();

    @Mock
    private final MockIssue issue2 = new MockIssue();

    @Mock
    private final MockIssue issue3 = new MockIssue();

    @Mock
    private final MockIssueType issueType1 = new MockIssueType("ID", TYPE_NAME_1);

    @Mock
    private final MockApplicationUser applicationUser1 = new MockApplicationUser(NAME_1);

    @Mock
    private final MockApplicationUser applicationUser2 = new MockApplicationUser(NAME_2);

    private WhoNeedsHelpService service;

    @Before
    public void setup() {
        service = new WhoNeedsHelpService(searchService, avatarService);

        componentWorker = new MockComponentWorker();
        componentWorker.registerMock(JqlClauseBuilderFactory.class, new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(timeZoneManager)));
        componentWorker.init();
    }

    @Test
    public void testGetContextMap() throws Exception {
        URI avatarUri = new URI(AVATAR_URL);
        List<Issue> issueList = new ArrayList<>();
        issueList.add(issue1);
        issueList.add(issue2);
        issueList.add(issue3);

        when(searchService.searchOverrideSecurity(any(), any(), any())).thenReturn(searchResults);
        when(searchResults.getIssues()).thenReturn(issueList);

        when(issue1.getAssignee()).thenReturn(applicationUser1);
        when(issue2.getAssignee()).thenReturn(applicationUser1);
        when(issue3.getAssignee()).thenReturn(applicationUser2);
        when(issue1.getOriginalEstimate()).thenReturn(ESTIMATE_1);
        when(issue2.getOriginalEstimate()).thenReturn(ESTIMATE_2);
        when(issue3.getOriginalEstimate()).thenReturn(ESTIMATE_3);

        when(issue1.getIssueType()).thenReturn(issueType1);
        when(issueType1.getName()).thenReturn(TYPE_NAME_1);
        when(issueType1.getCompleteIconUrl()).thenReturn(ICON_URL);

        when(applicationUser1.getName()).thenReturn(NAME_1);
        when(applicationUser2.getName()).thenReturn(NAME_2);

        when(avatarService.getAvatarUrlNoPermCheck(applicationUser1, Avatar.Size.SMALL)).thenReturn(avatarUri);
        when(avatarService.getAvatarUrlNoPermCheck(applicationUser2, Avatar.Size.SMALL)).thenReturn(avatarUri);

        List<Developer> developers = service.getSortedListOfDevelopersWithOpenIssues();

        assertEquals(developers.size(), 2);

        assertEquals(developers.get(0).getName(), NAME_2);
        assertEquals(developers.get(0).getAvatarUrl(), AVATAR_URL);
        assertEquals(developers.get(0).getOpenIssueCount(), new Integer(1));
        assertEquals(developers.get(0).getTotalOpenEstimate(), getHours(ESTIMATE_3));

        assertEquals(developers.get(1).getName(), NAME_1);
        assertEquals(developers.get(1).getAvatarUrl(), AVATAR_URL);
        assertEquals(developers.get(1).getOpenIssueCount(), new Integer(2));
        assertEquals(developers.get(1).getTotalOpenEstimate(), getHours(ESTIMATE_1 + ESTIMATE_2));

        assertEquals(developers.get(1).getOpenIssueTypes().size(), 1);
        assertEquals(developers.get(1).getOpenIssueTypes().get(0).getIssueCount(), new Integer(1));
        assertEquals(developers.get(1).getOpenIssueTypes().get(0).getCategoryName(), TYPE_NAME_1);
        assertEquals(developers.get(1).getOpenIssueTypes().get(0).getIconUrl(), ICON_URL);
    }

    private Long getHours(Long seconds) {
        return seconds / 3600;
    }
}

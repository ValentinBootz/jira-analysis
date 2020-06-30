package ut.org.pit.jira;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
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
import org.pit.jira.model.Filter;

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
    private final static String TYPE_NAME_2 = "test-type-name-2";
    private final static String TYPE_NAME_3 = "test-type-name-3";

    @Mock
    private SearchService searchService;

    @Mock
    private AvatarService avatarService;

    @Mock
    private PriorityManager priorityManager;

    @Mock
    private IssueTypeManager issueTypeManager;

    @Mock
    private SearchResults<Issue> searchResults;

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
    private final MockIssueType issueType1 = new MockIssueType("ID_1", TYPE_NAME_1);

    @Mock
    private final MockIssueType issueType2 = new MockIssueType("ID_2", TYPE_NAME_2);

    @Mock
    private final MockIssueType issueType3 = new MockIssueType("ID_3", TYPE_NAME_3);

    @Mock
    private final MockApplicationUser applicationUser1 = new MockApplicationUser(NAME_1);

    @Mock
    private final MockApplicationUser applicationUser2 = new MockApplicationUser(NAME_2);

    private WhoNeedsHelpService service;

    @Before
    public void setup() {
        service = new WhoNeedsHelpService(searchService, avatarService, priorityManager, issueTypeManager);

        componentWorker = new MockComponentWorker();
        componentWorker.registerMock(JqlClauseBuilderFactory.class, new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(timeZoneManager)));
        componentWorker.init();
    }

    @Test
    public void testGetSortedListOfDevelopersWithOpenIssues() throws Exception {
        URI avatarUri = new URI(AVATAR_URL);
        Filter filter = new Filter();

        List<Issue> issueList = new ArrayList<>();
        issueList.add(issue1);
        issueList.add(issue2);
        issueList.add(issue3);

        List<IssueType> issueTypeList = new ArrayList<>();
        issueTypeList.add(issueType3);
        issueTypeList.add(issueType2);
        issueTypeList.add(issueType1);

        when(searchService.searchOverrideSecurity(any(), any(), any())).thenReturn(searchResults);
        when(searchResults.getResults()).thenReturn(issueList);

        when(issueTypeManager.getIssueTypes()).thenReturn(issueTypeList);
        when(priorityManager.getPriorities()).thenReturn(new ArrayList<>());

        when(issue1.getAssignee()).thenReturn(applicationUser1);
        when(issue2.getAssignee()).thenReturn(applicationUser1);
        when(issue3.getAssignee()).thenReturn(applicationUser2);
        when(issue1.getOriginalEstimate()).thenReturn(ESTIMATE_1);
        when(issue2.getOriginalEstimate()).thenReturn(ESTIMATE_2);
        when(issue3.getOriginalEstimate()).thenReturn(ESTIMATE_3);

        when(issue1.getIssueType()).thenReturn(issueType1);
        when(issueType1.getName()).thenReturn(TYPE_NAME_1);
        when(issueType1.getCompleteIconUrl()).thenReturn(ICON_URL);

        when(issue2.getIssueType()).thenReturn(issueType2);
        when(issueType2.getName()).thenReturn(TYPE_NAME_2);
        when(issueType2.getCompleteIconUrl()).thenReturn(ICON_URL);

        when(applicationUser1.getName()).thenReturn(NAME_1);
        when(applicationUser2.getName()).thenReturn(NAME_2);

        when(avatarService.getAvatarUrlNoPermCheck(applicationUser1, Avatar.Size.SMALL)).thenReturn(avatarUri);
        when(avatarService.getAvatarUrlNoPermCheck(applicationUser2, Avatar.Size.SMALL)).thenReturn(avatarUri);

        List<Developer> developers = service.getSortedListOfDevelopersWithOpenIssues(filter);

        assertEquals(developers.size(), 2);

        assertEquals(NAME_2, developers.get(0).getName());
        assertEquals(AVATAR_URL, developers.get(0).getAvatar());
        assertEquals(new Integer(1), developers.get(0).getCount());
        assertEquals(ESTIMATE_3, developers.get(0).getTotalEstimateSeconds());

        assertEquals(NAME_1, developers.get(1).getName());
        assertEquals(AVATAR_URL, developers.get(1).getAvatar());
        assertEquals(new Integer(2), developers.get(1).getCount());
        assertEquals(new Long(ESTIMATE_1 + ESTIMATE_2), developers.get(1).getTotalEstimateSeconds());

        assertEquals(2, developers.get(1).getTypes().size());
        assertEquals(new Integer(1), developers.get(1).getTypes().get(0).getCount());
        assertEquals(new Integer(1), developers.get(1).getTypes().get(1).getCount());
        assertEquals(TYPE_NAME_2, developers.get(1).getTypes().get(0).getName());
        assertEquals(ICON_URL, developers.get(1).getTypes().get(0).getIconUrl());
        assertEquals(TYPE_NAME_1, developers.get(1).getTypes().get(1).getName());
        assertEquals(ICON_URL, developers.get(1).getTypes().get(1).getIconUrl());
    }
}

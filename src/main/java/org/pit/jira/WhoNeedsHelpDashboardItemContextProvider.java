package org.pit.jira;

import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.pit.jira.model.Developer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The context data provider for Who Needs Help Dashboard Item.
 */
@Scanned
@Slf4j
public class WhoNeedsHelpDashboardItemContextProvider implements ContextProvider {

    private final JiraAuthenticationContext authenticationContext;

    private final SearchService searchService;

    private final AvatarService avatarService;

    public WhoNeedsHelpDashboardItemContextProvider(@ComponentImport JiraAuthenticationContext authenticationContext,
                                                    @ComponentImport SearchService searchService,
                                                    @ComponentImport AvatarService avatarService) {
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
        this.avatarService = avatarService;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException {
    }

    /**
     * Initializes a context map with required entries for the dashboard item.
     *
     * @param context the context map
     * @return the initialized context map
     */
    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context) {
        final Map<String, Object> newContext = Maps.newHashMap(context);

        newContext.put("developers", getSortedListOfDevelopersWithOpenIssues());

        return newContext;
    }

    /**
     * Constructs a sorted list of developers with open issues.
     *
     * @return sorted list of developers
     */
    private List<Developer> getSortedListOfDevelopersWithOpenIssues() {
        List<Developer> developers = new ArrayList<>();
        List<Issue> issues = searchOpenAssignedIssues();

        issues.forEach(issue -> {
            ApplicationUser assignee = issue.getAssignee();
            Developer developer = developers.stream()
                    .filter(dev -> assignee.getName().equals(dev.getName()))
                    .findAny()
                    .orElse(null);

            if (developer != null) {
                // Increment open issue count.
                developer.setOpenIssueCount(developer.getOpenIssueCount() + 1);
            } else {
                // Create and add new developer.
                developer = createNewDeveloper(assignee);
                developers.add(developer);
            }
        });

        // Sort developer list.
        developers.sort(Comparator.comparing(Developer::getOpenIssueCount));

        return developers;
    }

    /**
     * Searches for open assigned issues.
     *
     * @return a list of open assigned issues
     */
    private List<Issue> searchOpenAssignedIssues() {
        List<Issue> issues = new ArrayList<>();

        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where()
                .not().assigneeIsEmpty()
                .and()
                .not().status(StatusCategory.COMPLETE)
                .endWhere();

        try {
            SearchResults result = searchService.searchOverrideSecurity(null, builder.buildQuery(), PagerFilter.getUnlimitedFilter());
            issues = result.getIssues();
        } catch (SearchException e) {
            log.error("Failed to search for open assigned issues", e);
        }

        return issues;
    }

    /**
     * Creates a new developer object and sets the number of assigned open issues to 1.
     *
     * @param assignee the assignee of the issue
     * @return a new developer object
     */
    private Developer createNewDeveloper(ApplicationUser assignee) {
        Developer developer = new Developer();

        developer.setName(assignee.getName());
        developer.setAvatarUrl(avatarService.getAvatarURL(authenticationContext.getLoggedInUser(), assignee));
        developer.setOpenIssueCount(1);

        return developer;
    }
}

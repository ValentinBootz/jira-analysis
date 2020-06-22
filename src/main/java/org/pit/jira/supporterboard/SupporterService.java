package org.pit.jira.supporterboard;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import lombok.extern.slf4j.Slf4j;
import org.ofbiz.core.entity.GenericValue;
import org.pit.jira.model.Developer;
import org.pit.jira.model.Filter;
import org.pit.jira.model.IssueCategory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The service for Supporter Dashboard Item.
 */
@Slf4j
@Scanned
@Component
public class SupporterService {

    private final SearchService searchService;

    private final AvatarService avatarService;

    private final PriorityManager priorityManager;

    private final IssueTypeManager issueTypeManager;


    @Inject
    public SupporterService(@ComponentImport SearchService searchService,
                            @ComponentImport AvatarService avatarService,
                            @ComponentImport PriorityManager priorityManager,
                            @ComponentImport IssueTypeManager issueTypeManager) {
        this.searchService = searchService;
        this.avatarService = avatarService;
        this.priorityManager = priorityManager;
        this.issueTypeManager = issueTypeManager;
    }

    /**
     * Constructs a sorted list of developers with the most reviewed issues.
     *
     * @param filter the filter object
     * @return sorted list of developers
     */


    public List<Developer> getSortedListOfDevelopersWithMostReviewedIssues(Filter filter) {
        List<Developer> developers = new ArrayList<>();
        List<Issue> issues = searchAllAssignedIssues(filter);

        for (Issue issue : issues) {
            boolean foundUser = false;
            List<ChangeHistory> changeHistories = ComponentAccessor.getChangeHistoryManager().getChangeHistories(issue);
            for (ChangeHistory change : changeHistories) {
                List<GenericValue> changeItems = change.getChangeItems();
                for (GenericValue item : changeItems) {
                    if (item.getString("oldstring") != null && item.getString("oldstring").toString().toLowerCase().contains("review")) {
                        String authorDisplayName = change.getAuthorDisplayName();

                        String userName = authorDisplayName.toString().toLowerCase();
                        if (!filter.getUsers().contains(userName))
                            continue;
                        for (Developer developer : developers) {
                            if (userName.equals(developer.getName())) {
                                foundUser = true;
                                // Update data for existing developer.
                                developer.setCount(developer.getCount() + 1);
                                setIssueType(developer, issue);
                                setIssuePriority(developer, issue);
                                developer.setTotalEstimateSeconds(developer.getTotalEstimateSeconds() + getEstimate(issue));
                            }
                        }
                        if (Boolean.FALSE.equals(foundUser)) {
                            // Create and add new developer.
                            UserManager userManager = ComponentAccessor.getComponent(UserManager.class);
                            ApplicationUser applicationUser = userManager.getUserByName(userName);

                            Developer developer = createNewDeveloper(applicationUser, issue);
                            developers.add(developer);
                        }
                    }

                }

            }

        }

        // Sort developer list.
        developers.sort(Comparator.comparing(Developer::getCount).reversed());

        // Remove empty issue categories.
        developers.forEach(developer -> developer.getTypes().removeIf(type -> type.getCount() == 0));
        developers.forEach(developer -> developer.getPriorities().removeIf(priority -> priority.getCount() == 0));

        return developers;
    }

    /**
     * Searches for Reviewed issues. If the filter has been defined, apply it to the search query.
     *
     * @param filter the filter object
     * @return a list of reviewed issues
     */
    private List<Issue> searchAllAssignedIssues(Filter filter) {
        List<Issue> issues;

        // Construct the JQL query.
        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        JqlClauseBuilder clauseBuilder = queryBuilder.where();
        applyFilter(clauseBuilder, filter);
        clauseBuilder.endWhere();

        try {
            SearchResults result = searchService.searchOverrideSecurity(null, queryBuilder.buildQuery(), PagerFilter.getUnlimitedFilter());
            issues = result.getIssues();
        } catch (SearchException e) {
            log.error("Failed to search for open assigned issues", e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return issues;
    }


    /**
     * Apply the filter to the JQL query.
     *
     * @param builder the JQL clause builder
     * @param filter  the filter object
     */
    private void applyFilter(JqlClauseBuilder builder, Filter filter) {
        if (filter.getUsers() != null && filter.getUsers().size() > 0) {
            builder.and().assignee().inStrings(filter.getUsers());
        }

        if (filter.getProjects() != null && filter.getProjects().size() > 0) {
            builder.and().project().inStrings(filter.getProjects());
        }

        if (filter.getTypes() != null && filter.getTypes().size() > 0) {
            builder.and().issueType().inStrings(filter.getTypes());
        }

        if (filter.getPriorities() != null && filter.getPriorities().size() > 0) {
            builder.and().priority().inStrings(filter.getPriorities());
        }
    }

    /**
     * Creates a new developer object and sets the number of assigned open issues to 1.
     *
     * @param assignee the assignee of the issue
     * @return a new developer object
     */
    private Developer createNewDeveloper(ApplicationUser assignee, Issue issue) {
        Developer developer = new Developer();

        developer.setName(assignee.getName());
        developer.setAvatar(getAvatarUrl(assignee));
        developer.setCount(1);
        developer.setTypes(getSortedBlankIssueTypes());
        developer.setPriorities(getSortedBlankPriorities());
        developer.setTotalEstimateSeconds(getEstimate(issue));

        setIssueType(developer, issue);
        setIssuePriority(developer, issue);

        return developer;
    }

    /**
     * Creates a list of all available blank issue type categories, sorted based on the order received from Jira.
     *
     * @return a sorted list of blank issue types
     */
    private List<IssueCategory> getSortedBlankIssueTypes() {
        List<IssueCategory> blankIssueTypes = new ArrayList<>();
        List<IssueType> sortedIssueTypes = new ArrayList<>(issueTypeManager.getIssueTypes());

        sortedIssueTypes.forEach(issueType -> {
            IssueCategory category = getBlankIssueCategory(issueType.getName(), issueType.getCompleteIconUrl());
            blankIssueTypes.add(category);
        });

        return blankIssueTypes;
    }

    /**
     * Creates a list of all available blank priority categories, sorted based on the order received from Jira.
     *
     * @return a sorted list of blank priorities
     */
    private List<IssueCategory> getSortedBlankPriorities() {
        List<IssueCategory> blankPriorities = new ArrayList<>();
        List<Priority> sortedPriorities = priorityManager.getPriorities();

        sortedPriorities.forEach(priority -> {
            IssueCategory category = getBlankIssueCategory(priority.getName(), priority.getCompleteIconUrl());
            blankPriorities.add(category);
        });

        return blankPriorities;
    }

    /**
     * Creates a blank issue category (issue category count set to 0).
     *
     * @param categoryName the name of the category
     * @param iconUrl      the icon URL
     * @return a blank issue category
     */
    private IssueCategory getBlankIssueCategory(String categoryName, String iconUrl) {
        IssueCategory category = new IssueCategory();
        category.setName(categoryName);
        category.setIconUrl(iconUrl);
        category.setCount(0);

        return category;
    }

    /**
     * Returns the estimate for the issue in seconds. If an estimate has not been defined, returns 0.
     *
     * @param issue the issue
     * @return the estimate in seconds
     */
    private Long getEstimate(Issue issue) {
        Long estimate = issue.getOriginalEstimate();

        return estimate != null ? estimate : 0L;
    }

    /**
     * Set the issue type for the developer.
     *
     * @param developer the developer
     * @param issue     the issue
     */
    private void setIssueType(Developer developer, Issue issue) {
        IssueType issueType = issue.getIssueType();

        if (issueType != null) {
            updateIssueCategories(developer.getTypes(), issueType.getName());
        }
    }

    /**
     * Set the issue priority for the developer.
     *
     * @param developer the developer
     * @param issue     the issue
     */
    private void setIssuePriority(Developer developer, Issue issue) {
        Priority priority = issue.getPriority();

        if (priority != null) {
            updateIssueCategories(developer.getPriorities(), priority.getName());
        }
    }

    /**
     * Update the developers list of issue categories.
     *
     * @param categoryList the existing list of issue categories of the developer
     * @param categoryName the name of the issue category to be added
     */
    private void updateIssueCategories(List<IssueCategory> categoryList, String categoryName) {
        // Increment the issue count for the category.
        categoryList.stream()
                .filter(cat -> categoryName.equals(cat.getName()))
                .findAny().ifPresent(existingCategory ->
                existingCategory.setCount(existingCategory.getCount() + 1));

    }

    /**
     * Retrieve the URL for the small avatar of the assignee
     *
     * @param assignee the assignee
     * @return the avatar URL as String
     */
    private String getAvatarUrl(ApplicationUser assignee) {
        String avatarUrl = "";

        try {
            avatarUrl = avatarService.getAvatarUrlNoPermCheck(assignee, Avatar.Size.SMALL).toURL().toString();
        } catch (MalformedURLException e) {
            log.error("Failed to retrieve avatar URL", e);
        }

        return avatarUrl;
    }
}

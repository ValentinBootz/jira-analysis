package org.pit.jira.help;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import lombok.extern.slf4j.Slf4j;
import org.pit.jira.model.Developer;
import org.pit.jira.model.IssueCategory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The service for Who Needs Help Dashboard Item.
 */
@Slf4j
@Scanned
@Component
public class WhoNeedsHelpService {

    private final SearchService searchService;

    private final AvatarService avatarService;

    private final PriorityManager priorityManager;

    private final IssueTypeManager issueTypeManager;

    @Inject
    public WhoNeedsHelpService(@ComponentImport SearchService searchService,
                               @ComponentImport AvatarService avatarService,
                               @ComponentImport PriorityManager priorityManager,
                               @ComponentImport IssueTypeManager issueTypeManager) {
        this.searchService = searchService;
        this.avatarService = avatarService;
        this.priorityManager = priorityManager;
        this.issueTypeManager = issueTypeManager;
    }

    /**
     * Constructs a sorted list of developers with open issues.
     *
     * @return sorted list of developers
     */
    public List<Developer> getSortedListOfDevelopersWithOpenIssues() {
        List<Developer> developers = new ArrayList<>();
        List<Issue> issues = searchOpenAssignedIssues();

        issues.forEach(issue -> {
            ApplicationUser assignee = issue.getAssignee();
            Developer developer = developers.stream()
                    .filter(dev -> assignee.getName().equals(dev.getName()))
                    .findAny()
                    .orElse(null);

            if (developer != null) {
                // Update data for existing developer.
                developer.setOpenIssueCount(developer.getOpenIssueCount() + 1);
                setIssueType(developer, issue);
                setIssuePriority(developer, issue);
                developer.setTotalOpenEstimate(developer.getTotalOpenEstimate() + getEstimate(issue));
            } else {
                // Create and add new developer.
                developer = createNewDeveloper(assignee, issue);
                developers.add(developer);
            }
        });

        // Sort developer list.
        developers.sort(Comparator.comparing(Developer::getOpenIssueCount));

        // Remove empty issue categories.
        developers.forEach(developer -> developer.getOpenIssueTypes().removeIf(type -> type.getIssueCount() == 0));
        developers.forEach(developer -> developer.getOpenIssuePriorities().removeIf(priority -> priority.getIssueCount() == 0));

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
    private Developer createNewDeveloper(ApplicationUser assignee, Issue issue) {
        Developer developer = new Developer();

        developer.setName(assignee.getName());
        developer.setAvatarUrl(getAvatarUrl(assignee));
        developer.setOpenIssueCount(1);
        developer.setOpenIssueTypes(getSortedBlankIssueTypes());
        developer.setOpenIssuePriorities(getSortedBlankPriorities());
        developer.setTotalOpenEstimate(getEstimate(issue));

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
        category.setCategoryName(categoryName);
        category.setIconUrl(iconUrl);
        category.setIssueCount(0);

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
            updateIssueCategories(developer.getOpenIssueTypes(), issueType.getName());
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
            updateIssueCategories(developer.getOpenIssuePriorities(), priority.getName());
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
                .filter(cat -> categoryName.equals(cat.getCategoryName()))
                .findAny().ifPresent(existingCategory ->
                existingCategory.setIssueCount(existingCategory.getIssueCount() + 1));

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
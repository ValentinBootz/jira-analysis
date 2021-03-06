package org.pit.jira.access;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.pit.jira.model.AccessRequest;
import org.pit.jira.model.Grant;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * The service for the Logging and Access API.
 */
@Slf4j
@Scanned
@Component
public class LoggingAndAccessService {

    private final static String JIRA = "jira";
    private final static String EXPERT_JUSTIFICATION = "The Expert Dashboard Item is used to look for developers who have already worked on a specific topic, we call those developers experts.";
    private final static String LEADERBOARD_JUSTIFICATION = "The Leaderboard Item displays lists of users and projects that provide insights on productivity.";
    private final static String HELP_JUSTIFICATION = "The Who Needs Help Dashboard Item provides information about the current workload of the developers in a team.";
    private final static String SUPPORTER_JUSTIFICATION = "The Supporter Dashboard Item provides information about the number of issues developers have reviewed.";

    private final static String USERNAME = "techie";
    private final static String PASSWORD = "some_body_00";
    private final static String URL = "https://overseer.sse.in.tum.de/request-access/query";

    private final JiraAuthenticationContext jiraAuthenticationContext;

    private final UserManager userManager;

    private final UserSearchService userSearchService;

    @Inject
    public LoggingAndAccessService(@ComponentImport JiraAuthenticationContext jiraAuthenticationContext,
                                   @ComponentImport UserManager userManager,
                                   @ComponentImport UserSearchService userSearchService) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userManager = userManager;
        this.userSearchService = userSearchService;
    }

    /**
     * Requests query access from the Logging and Access API.
     *
     * @param itemType       the type of the dashboard item
     * @param ownerUsernames a list of data owner usernames
     * @return a grant with granted true or granted false
     */
    public Grant requestQueryAccess(String itemType, List<String> ownerUsernames) {
        AccessRequest accessRequest = constructAccessRequest(itemType, ownerUsernames);

        // Set up credentials.
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD);
        provider.setCredentials(AuthScope.ANY, credentials);

        // Set up HTTP client.
        HttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();

        // Conversion between JSON and Java objects.
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        Gson gson = gsonBuilder.create();

        HttpPost httpPost = constructRequest(gson, accessRequest);

        try {
            // Execute request.
            HttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
                log.error("Received HTTP status: " + response.getStatusLine().getStatusCode()
                        + " (" + response.getStatusLine().getReasonPhrase() + ")");
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            } else {
                return gson.fromJson(EntityUtils.toString(response.getEntity()), Grant.class);
            }
        } catch (IOException e) {
            log.error("Failed to execute HTTP POST request.", e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Constructs a HTTP POST request.
     *
     * @param gson          the JSON converter
     * @param accessRequest the access request
     * @return a HTTP POST request
     */
    private HttpPost constructRequest(Gson gson, AccessRequest accessRequest) {
        try {
            HttpPost httpPost = new HttpPost(URL);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            StringEntity body = new StringEntity(gson.toJson(accessRequest));
            httpPost.setEntity(body);

            return httpPost;
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to construct HTTP POST request.", e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Constructs an access request.
     *
     * @param itemType       the type of the dashboard item
     * @param ownerUsernames a list of data owner usernames
     * @return an access request
     */
    private AccessRequest constructAccessRequest(String itemType, List<String> ownerUsernames) {
        AccessRequest accessRequest = new AccessRequest();
        List<String> ownerEmails = getOwnerEmails(ownerUsernames);
        ApplicationUser currentUser = jiraAuthenticationContext.getLoggedInUser();

        accessRequest.setUser(currentUser.getEmailAddress());
        accessRequest.setOwners(ownerEmails);
        accessRequest.setTool(JIRA);

        List<String> dataTypes = new ArrayList<>();

        if (itemType.equals(ItemType.EXPERT.getItemType())) {
            // Expert Dashboard Item.
            dataTypes.add(DataType.ISSUE_LINK.getDataType());
            dataTypes.add(DataType.ISSUE_TITLE.getDataType());
            dataTypes.add(DataType.ISSUE_SUMMARY.getDataType());

            accessRequest.setJustification(EXPERT_JUSTIFICATION);
        } else if (itemType.equals(ItemType.LEADERBOARD.getItemType())) {
            // Leaderboard Dashboard Item.
            addCommonBoardDataTypes(dataTypes);

            accessRequest.setJustification(LEADERBOARD_JUSTIFICATION);
        } else if (itemType.equals(ItemType.HELP.getItemType())) {
            // Who Needs Help Dashboard Item.
            addCommonBoardDataTypes(dataTypes);

            accessRequest.setJustification(HELP_JUSTIFICATION);
        } else if (itemType.equals(ItemType.SUPPORTER.getItemType())) {
            // Supporter Dashboard Item.
            addCommonBoardDataTypes(dataTypes);

            accessRequest.setJustification(SUPPORTER_JUSTIFICATION);
        } else {
            log.error("Non-existent Dashboard Item type: " + itemType);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        addCommonDataTypes(dataTypes);
        accessRequest.setDataTypes(dataTypes);

        return accessRequest;
    }

    /**
     * Retrieves the email addresses of the data owners.
     * If a filter has been defined, then retrieves only emails for usernames in the filter, else retrieves all emails
     * of active users.
     *
     * @param ownerUsernames a list of data owner usernames
     * @return a list of data owner emails
     */
    private List<String> getOwnerEmails(List<String> ownerUsernames) {
        List<String> ownerEmails = new ArrayList<>();

        if (ownerUsernames != null && ownerUsernames.size() > 0) {
            // Retrieve filtered user emails.
            ownerUsernames.forEach(username -> {
                ApplicationUser user = userManager.getUserByName(username);
                if (user != null) {
                    ownerEmails.add(user.getEmailAddress());
                }
            });
        } else {
            // If no filter defined, retrieve all user emails.
            JiraServiceContext serviceContext = new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser());
            List<ApplicationUser> users = userSearchService.findUsersAllowEmptyQuery(serviceContext, "");
            users.forEach(user -> ownerEmails.add(user.getEmailAddress()));
        }

        return ownerEmails;
    }

    /**
     * Adds accessed data types common to all of the dashboard items.
     *
     * @param dataTypes the data types
     */
    private void addCommonDataTypes(List<String> dataTypes) {
        dataTypes.add(DataType.ISSUE_STATUS.getDataType());
        dataTypes.add(DataType.ASSIGNEE_NAME.getDataType());
        dataTypes.add(DataType.ASSIGNEE_EMAIL.getDataType());
        dataTypes.add(DataType.ASSIGNEE_AVATAR.getDataType());
    }

    /**
     * Adds accessed data types common to all board dashboard items.
     *
     * @param dataTypes the data types
     */
    private void addCommonBoardDataTypes(List<String> dataTypes) {
        dataTypes.add(DataType.ISSUE_TYPE.getDataType());
        dataTypes.add(DataType.ISSUE_PRIORITY.getDataType());
        dataTypes.add(DataType.ISSUE_ESTIMATE.getDataType());
    }
}

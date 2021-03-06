package org.pit.jira.leaderboard;

import org.springframework.stereotype.Component;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.utils.URIBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

@Slf4j
@Scanned
@Component
public class LeaderboardService {

    /**
     * Requests completed issues that match JQL query from Jira API and filters issues matching the specified users.
     *
     * @return completed issues
     */
    public String getCompletedIssues(String JSESSIONID, String base_url, String jql_query, List<String> users) throws IOException, URISyntaxException {
        HttpClient client = HttpClientBuilder.create().build();

        URIBuilder uri_builder = new URIBuilder(base_url + "/rest/api/2/search")
                .addParameter("jql", jql_query)
                .addParameter("maxResults", "1000")
                .addParameter("expand", "changelog");

        HttpGet request = new HttpGet(uri_builder.toString());
        request.setHeader("Cookie", "JSESSIONID=" + JSESSIONID);

        HttpResponse HttpResponse = client.execute(request);

        log.info("Received HTTP status: " + HttpResponse.getStatusLine().getStatusCode()
                + " (" + HttpResponse.getStatusLine().getReasonPhrase() + ")");

        return EntityUtils.toString(HttpResponse.getEntity());

        // JSONObject response = new JSONObject(EntityUtils.toString(HttpResponse.getEntity()));

        // return filterUsers(response, users);
    }

    /**
     * Filters issues where users contains the developer.
     */
    private String filterUsers(JSONObject response, List<String> users) {
        JSONArray result = new JSONArray();
        JSONArray issues = response.getJSONArray("issues");

        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            JSONObject developer = getDeveloper(issue.getJSONObject("changelog"));

            if (developer != null && users.contains(developer.getString("name"))) {
                issue.put("developer", developer);
                result.put(issue);
            }
        }

        return result.toString();
    }

    /**
     * Gets developer name from issue changelog.
     */
    private JSONObject getDeveloper(JSONObject changelog) {
        JSONObject last = new JSONObject();
        JSONArray histories = changelog.getJSONArray("histories");
        for (int i = 0; i < histories.length(); i++) {
            JSONObject history = histories.getJSONObject(i);
            JSONArray items = history.getJSONArray("items");
            if (containsInProgressTransition(items)) {
                last = history;
            }
        }
        if (last.has("author")) {
            return last.getJSONObject("author");
        } else {
            return null;
        }
    }

    /**
     * Checks if history items contain transition to 'In Progress'.
     */
    private boolean containsInProgressTransition(JSONArray items) {
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            try {
                String toString = item.getString("toString");
                if (toString.equals("In Progress")) {
                    return true;
                }
            } catch (JSONException e) {
                log.info("No toString status found for item: " + item.toString());
            }
        }

        return false;
    }
}
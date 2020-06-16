package org.pit.jira.leaderboard;

import org.springframework.stereotype.Component;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

@Scanned
@Component
public class LeaderboardService {

    public String getIssues(String JSESSIONID, String base_url, String jql_query, List<String> users) throws IOException, URISyntaxException {
        
        HttpClient client = HttpClientBuilder.create()
            .build();

        URIBuilder uri_builder = new URIBuilder(base_url + "/rest/api/2/search")
            .addParameter("jql", jql_query)
            .addParameter("expand", "changelog");

        HttpGet request = new HttpGet(uri_builder.toString());
        request.setHeader("Cookie", "JSESSIONID=" + JSESSIONID);

        HttpResponse response = client.execute(request);

        return filterUsers(new JSONObject(EntityUtils.toString(response.getEntity())), users);
    }

    private String filterUsers(JSONObject response, List<String> users) {
        JSONArray result = new JSONArray();
        JSONArray issues = response.getJSONArray("issues");
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            if (users.contains(getDeveloper(issue.getJSONObject("changelog")))) {
                result.put(issue);
            }
        }
        return result.toString();
    }

    private String getDeveloper(JSONObject changelog) {
        JSONObject last = new JSONObject();
        JSONArray histories = changelog.getJSONArray("histories");
        for (int i = 0; i < histories.length(); i++) {
            JSONObject history = histories.getJSONObject(i);
            JSONArray items = history.getJSONArray("items");
            if (containsInProgressTransition(items)) {
                last = history;
            }
        }
        if(last.has("author")){
            JSONObject author = last.getJSONObject("author");
            return "\"" + author.getString("name") + "\"";
        } else {
            return null;
        }
    }

    private boolean containsInProgressTransition(JSONArray items) {
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.getString("toString").equals("In Progress")) {
                return true;
            }
        }
        return false;
    }
}
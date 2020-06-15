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

@Scanned
@Component
public class LeaderboardService {

    public String getIssues(String JSESSIONID, String base_url, String jql_query) throws IOException, URISyntaxException {
        
        HttpClient client = HttpClientBuilder.create()
            .build();

        URIBuilder uri_builder = new URIBuilder(base_url + "/rest/api/2/search")
            .addParameter("jql", jql_query)
            .addParameter("expand", "changelog");

        HttpGet request = new HttpGet(uri_builder.toString());
        request.setHeader("Cookie", "JSESSIONID=" + JSESSIONID);

        HttpResponse response = client.execute(request);
        return EntityUtils.toString(response.getEntity());
    }
}
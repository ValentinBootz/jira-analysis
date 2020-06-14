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
import java.io.IOException;

@Scanned
@Component
public class LeaderboardService {

    public String getIssues(String url) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(new HttpGet(url));
        return EntityUtils.toString(response.getEntity());
    }
}
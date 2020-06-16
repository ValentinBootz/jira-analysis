package org.pit.jira.leaderboard;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

@Path("/leaderboard")
public class LeaderboardResource {

    @GET
    @Produces("text/plain")
    @Path("/issues")
    public String getIssues() {
        return "Hello World";
    }
}
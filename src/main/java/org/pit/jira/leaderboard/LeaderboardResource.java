package org.pit.jira.leaderboard;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/leaderboard")
public class LeaderboardResource {

    private final LeaderboardService leaderboardService;

    public LeaderboardResource(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/issues")
    public Response getIssues(@CookieParam("JSESSIONID") Cookie cookie, @QueryParam("base_url") String base_url, @QueryParam("jql_query") String jql_query) {
        try {
            return Response.status(Status.OK).entity(leaderboardService.getIssues(cookie.getValue(), base_url, jql_query)).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build(); 
        }
    }
}
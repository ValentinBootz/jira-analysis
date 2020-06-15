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

import org.pit.jira.access.LoggingAndAccessService;
import org.pit.jira.model.Grant;
import java.util.List;

@Path("/leaderboard")
public class LeaderboardResource {

    private final LeaderboardService leaderboardService;
    private final LoggingAndAccessService loggingAndAccessService;

    public LeaderboardResource(LoggingAndAccessService loggingAndAccessService, LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
        this.loggingAndAccessService = loggingAndAccessService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/issues")
    public Response getIssues(@CookieParam("JSESSIONID") Cookie cookie, @QueryParam("base_url") String base_url, @QueryParam("jql_query") String jql_query, @QueryParam("owners") List<String> owners) {

        Grant grant = loggingAndAccessService.requestQueryAccess("leaderboard", owners);
        
        if(grant.granted) {
            try {
                return Response.status(Status.OK).entity(leaderboardService.getIssues(cookie.getValue(), base_url, jql_query)).build();
            } catch (Exception e) {
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build(); 
            }
        } else {
            return Response.status(Status.FORBIDDEN).build(); 
        }
    }
}
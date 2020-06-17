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
import javax.inject.Inject;

import org.pit.jira.access.LoggingAndAccessService;
import org.pit.jira.model.Grant;
import org.pit.jira.access.ItemType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/leaderboard")
public class LeaderboardResource {

    private final LeaderboardService leaderboardService;
    private final LoggingAndAccessService loggingAndAccessService;

    @Inject
    public LeaderboardResource(LoggingAndAccessService loggingAndAccessService, LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
        this.loggingAndAccessService = loggingAndAccessService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/issues")
    public Response getCompletedIssues(@CookieParam("JSESSIONID") Cookie cookie, @QueryParam("base_url") String base_url, @QueryParam("jql_query") String jql_query, @QueryParam("users") List<String> users) {

        try {
            log.info("Requesting query access for " + ItemType.LEADERBOARD.getItemType() + " item.");
            Grant grant = loggingAndAccessService.requestQueryAccess(ItemType.LEADERBOARD.getItemType(), users);
            if(grant.getGranted()) {
                    log.info("Access to issues granted for " + ItemType.LEADERBOARD.getItemType() + " item.");
                    return Response.status(Status.OK).entity(leaderboardService.getCompletedIssues(cookie.getValue(), base_url, jql_query, users)).build();
            } else {
                log.info("Access to issues not granted for " + ItemType.LEADERBOARD.getItemType() + " item.");
                return Response.status(Status.FORBIDDEN).build(); 
            }
        } catch (Exception e) {
            log.error("Internal server error during data retrieval.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build(); 
        }
    }
}
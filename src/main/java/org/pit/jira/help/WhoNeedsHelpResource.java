package org.pit.jira.help;

import lombok.extern.slf4j.Slf4j;
import org.pit.jira.access.ItemType;
import org.pit.jira.access.LoggingAndAccessService;
import org.pit.jira.model.Developer;
import org.pit.jira.model.Filter;
import org.pit.jira.model.Grant;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * The resource for Who Needs Help Dashboard Item.
 */
@Slf4j
@Path("/who-needs-help")
public class WhoNeedsHelpResource {

    private final WhoNeedsHelpService whoNeedsHelpService;

    private final LoggingAndAccessService loggingAndAccessService;

    @Inject
    public WhoNeedsHelpResource(WhoNeedsHelpService whoNeedsHelpService, LoggingAndAccessService loggingAndAccessService) {
        this.whoNeedsHelpService = whoNeedsHelpService;
        this.loggingAndAccessService = loggingAndAccessService;
    }

    /**
     * Searches for developers and their open issues.
     *
     * @param filter the filter object
     * @return a sorted list of developers with their open issues
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/issues")
    public Response getDevelopersWithOpenIssues(Filter filter) {
        try {
            Grant grant = loggingAndAccessService.requestQueryAccess(ItemType.HELP.getItemType(), filter.getUsers());

            if (grant.getGranted()) {
                // Access granted.
                log.info("Item " + ItemType.HELP.getItemType() + " requesting developers with open issues.");

                List<Developer> developers = whoNeedsHelpService.getSortedListOfDevelopersWithOpenIssues(filter);

                log.info("Item " + ItemType.HELP.getItemType() + " received " + developers.size() + " developers with open issues.");

                return Response.ok(developers).build();
            } else {
                // Access not granted.
                log.info("Access to developers with open issues not granted for " + ItemType.HELP.getItemType() + " item.");
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (WebApplicationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}

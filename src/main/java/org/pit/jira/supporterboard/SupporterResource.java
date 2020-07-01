package org.pit.jira.supporterboard;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
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
 * The resource for Supporter Dashboard Item.
 */
@Slf4j
@Path("/supporter")
public class SupporterResource {

    private final SupporterService supporterService;

    private final LoggingAndAccessService loggingAndAccessService;

    @Inject
    public SupporterResource(SupporterService supporterService, LoggingAndAccessService loggingAndAccessService) {
        this.supporterService = supporterService;
        this.loggingAndAccessService = loggingAndAccessService;
    }

    /**
     * Searches for developers and number of issues they've reviewed.
     *
     * @param filter the filter object
     * @return a sorted list of developers with number of issues they've reviewed
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/reviewers")
    public Response getDevelopersWithOpenIssues(Filter filter) {
        try {
            Grant grant = loggingAndAccessService.requestQueryAccess(ItemType.SUPPORTER.getItemType(), filter.getUsers());

            if (grant.getGranted()) {
                // Access granted.
                log.info("Item " + ItemType.SUPPORTER.getItemType() + " requesting developers with the most reviewed issues.");

                List<Developer> developers = supporterService.getSortedListOfDevelopersWithMostReviewedIssues(filter);

                log.info("Item " + ItemType.SUPPORTER.getItemType() + " received " + developers.size() + " developers with the most reviewed issues.");

                return Response.ok(developers).build();
            } else {
                // Access not granted.
                log.info("Access to developers with the most reviewed issues not granted for " + ItemType.SUPPORTER.getItemType() + " item.");
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (WebApplicationException e) {
            log.error("Internal server error during data retrieval.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getFullStackTrace(e)).build();
        }
    }
}

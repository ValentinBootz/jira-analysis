package org.pit.jira.access;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.pit.jira.model.Grant;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * The resource for the Logging and Access API.
 */
@Slf4j
@Path("/logging-and-access")
public class LoggingAndAccessResource {

    private final LoggingAndAccessService loggingAndAccessService;

    @Inject
    public LoggingAndAccessResource(LoggingAndAccessService loggingAndAccessService) {
        this.loggingAndAccessService = loggingAndAccessService;
    }

    /**
     * Requests query access from the Logging and Access API.
     *
     * @param itemType       the type of the dashboard item
     * @param ownerUsernames a list of data owner usernames
     * @return a grant with granted "true" or granted "false"
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/query/{itemType}")
    public Response requestQueryAccess(@PathParam("itemType") String itemType, List<String> ownerUsernames) {
        try {
            log.info("Requesting query access for " + itemType + " item.");

            Grant grant = loggingAndAccessService.requestQueryAccess(itemType, ownerUsernames);

            log.info("Received granted \"" + grant.getGranted() + "\" for the " + itemType + " item.");

            return Response.ok(grant).build();
        } catch (WebApplicationException e) {
            log.error("Internal server error during data retrieval.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ExceptionUtils.getFullStackTrace(e)).build();
        }
    }
}

package org.pit.jira.help;

import lombok.extern.slf4j.Slf4j;
import org.pit.jira.model.Developer;
import org.pit.jira.model.Filter;

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

    @Inject
    public WhoNeedsHelpResource(WhoNeedsHelpService whoNeedsHelpService) {
        this.whoNeedsHelpService = whoNeedsHelpService;
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
        log.info("Requesting developers with open issues.");

        List<Developer> developers = whoNeedsHelpService.getSortedListOfDevelopersWithOpenIssues(filter);

        log.info("Received " + developers.size() + " developers with open issues.");

        return Response.ok(developers).build();
    }
}

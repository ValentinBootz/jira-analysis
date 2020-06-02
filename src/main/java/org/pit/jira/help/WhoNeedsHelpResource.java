package org.pit.jira.help;

import lombok.extern.slf4j.Slf4j;
import org.pit.jira.model.Developer;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
     * @return a sorted list of developers with their open issues
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/issues")
    public Response getDevelopersWithOpenIssues() {
        List<Developer> developers = whoNeedsHelpService.getSortedListOfDevelopersWithOpenIssues();

        return Response.ok(developers).build();
    }
}

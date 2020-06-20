package org.pit.jira.supporterboard;

import lombok.extern.slf4j.Slf4j;
import org.pit.jira.model.Developer;
import org.pit.jira.model.Filter;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * The resource for Supporter Dashboard Item.
 */
@Slf4j
@Path("/who-needs-help")
public class SupporterResource {

    private final SupporterService supporterService;

    @Inject
    public SupporterResource(SupporterService supporterService) {
        this.supporterService = supporterService;
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

        List<Developer> developers = supporterService.getSortedListOfDevelopersWithOpenIssues(filter);

        log.info("Received " + developers.size() + " developers with open issues.");

        return Response.ok(developers).build();
    }
}

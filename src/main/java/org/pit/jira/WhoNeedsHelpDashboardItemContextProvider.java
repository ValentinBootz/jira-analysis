package org.pit.jira;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * The context data provider for Who Needs Help Dashboard Item.
 */
@Scanned
public class WhoNeedsHelpDashboardItemContextProvider implements ContextProvider {

    public WhoNeedsHelpDashboardItemContextProvider() {
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException {
    }

    /**
     * Initializes a context map with required entries for the dashboard item.
     *
     * @param context the context map
     * @return the initialized context map
     */
    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context) {
        final Map<String, Object> newContext = Maps.newHashMap(context);

        // TODO

        return newContext;
    }
}

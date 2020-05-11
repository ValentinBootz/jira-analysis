package org.pit.jira;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * The context data provider for Leaderboard Dashboard Item.
 */
@Scanned
public class LeaderboardDashboardItemContextProvider implements ContextProvider {

    private final PluginAccessor pluginAccessor;

    public LeaderboardDashboardItemContextProvider(@ComponentImport PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
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
        Plugin plugin = pluginAccessor.getEnabledPlugin("org.pit.jira.dashboard-plugin");
        newContext.put("version", plugin.getPluginInformation().getVersion());
        newContext.put("pluginName", plugin.getName());

        return newContext;
    }
}

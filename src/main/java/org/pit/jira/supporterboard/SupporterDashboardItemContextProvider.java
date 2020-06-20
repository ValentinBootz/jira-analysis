//package org.pit.jira.supporterboard;
//
//import com.atlassian.plugin.PluginParseException;
//import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
//import com.atlassian.plugin.web.ContextProvider;
//
//import java.util.Map;
//
///**
// * The context data provider for Example Dashboard Item.
// */
//@Scanned
//public class SupporterDashboardItemContextProvider implements ContextProvider {
//
//    private final PluginAccessor pluginAccessor;
//
//    private final IssueManager issueManager;
//
//    public SupporterDashboardItemContextProvider(@ComponentImport PluginAccessor pluginAccessor,
//                                               @ComponentImport IssueManager issueManager) {
//        this.pluginAccessor = pluginAccessor;
//        this.issueManager = issueManager;
//    }
//
//    @Override
//    public void init(final Map<String, String> params) throws PluginParseException {
//    }
//
//    /**
//     * Initializes a context map with required entries for the dashboard item.
//     *
//     * @param context the context map
//     * @return the initialized context map
//     */
//    @Override
//    public Map<String, Object> getContextMap(final Map<String, Object> context) {
//        final Map<String, Object> newContext = Maps.newHashMap(context);
//        Plugin plugin = pluginAccessor.getEnabledPlugin("org.pit.jira.dashboard-plugin");
//        newContext.put("version", plugin.getPluginInformation().getVersion());
//        newContext.put("pluginName", plugin.getName());
//        newContext.put("issueCount", issueManager.getIssueCount());
//
//        return newContext;
//    }
//
//}

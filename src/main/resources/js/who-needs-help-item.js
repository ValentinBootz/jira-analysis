define('jira-dashboard-items/who-needs-help', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

    var DashboardItem = function (API) {
        this.API = API;
        this.developers = [];
    };

    /**
     * Called to render the view for a fully configured dashboard item.
     *
     * @param context The surrounding <div/> context that this item should render into
     * @param preferences The user preferences saved for this dashboard item (e.g. filter id, number of results...)
     */
    DashboardItem.prototype.render = function (context, preferences) {
        this.API.showLoadingBar();
        var $element = this.$element = $(context).find("#dynamic-content");
        var self = this;

        this.requestData().done(function (data) {
            self.API.hideLoadingBar();
            self.developers = data;

            if (self.developers === undefined || self.developers.length === 0) {
                $element.empty().html(Who.Needs.Help.Dashboard.Item.Templates.Empty());
            } else {
                $element.empty().html(Who.Needs.Help.Dashboard.Item.Templates.Results({developers: self.developers}));
            }

            self.API.resize();
        });

        this.API.once("afterRender", this.API.resize);
    }

    /**
     * Searches for open assigned issues.
     *
     * @returns {*} a list of open assigned issues
     */
    DashboardItem.prototype.requestData = function () {
        return $.ajax({
            type: "GET",
            url: contextPath() + "/rest/jira-analysis-api/1.0/who-needs-help/issues",
        });
    };

    return DashboardItem;
});
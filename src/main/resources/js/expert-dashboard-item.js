define('jira-dashboard-items/expert', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

    var DashboardItem = function (API) {
        this.API = API;
        this.issues = [];
    };
    /**
    * Called to render the view for a fully configured dashboard item.
    *
    * @param context The surrounding <div/> context that this item should render into.
    * @param preferences The user preferences saved for this dashboard item (e.g. filter id, number of results...)
    */
    DashboardItem.prototype.render = function (context, preferences) {
        this.API.showLoadingBar();
        var $element = this.$element = $(context).find("#dynamic-content");
        var self = this;
        this.requestData().done(function (data) {
            self.API.hideLoadingBar();
            self.issues = data.issues;
            
            /**
            * If response contains no issues use .Empty template
            */
            if (self.issues === undefined || self.issues.length  === 0) {
                $element.empty().html(Expert.Dashboard.Item.Templates.Empty());
            }
            /**
            * Otherwise populate array with data for .Search template
            */
            else {
                
               $element.empty().html(Expert.Dashboard.Item.Templates.Search());
            }
            self.API.resize();
            $element.find(".submit").click(function (event) {
                event.preventDefault();
                self.render(element, preferences);
            });
        });

        this.API.once("afterRender", this.API.resize);
    };

    /**
    * REST call requesting all issues with status 'Done' with expanded changelog
    */
    DashboardItem.prototype.requestData = function () {
        return $.ajax({
            method: "GET",
            url: contextPath() + "/rest/api/latest/search?jql=status%3Ddone&expand=changelog"
        });
    };

    
    return DashboardItem;
});

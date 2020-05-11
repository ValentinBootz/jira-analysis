define('jira-dashboard-items/leaderboard', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

    var DashboardItem = function (API) {
        this.API = API;
        this.issues = [];
    };
    /**
    * Called to render the view for a fully configured dashboard item.
    *
    * @param context The surrounding <div/> context that this items should render into.
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
                $element.empty().html(Leaderboard.Dashboard.Item.Templates.Empty());
            }
            /**
            * Otherwise populate leaderboard array with assignees of issues in response
            */
            else {
                self.leaderboard = [];
                self.issues.forEach(issue => {
                    var assignee = issue.fields.assignee;
                    name = assignee ? assignee.name : "Unassigned"
                    var index = self.leaderboard.findIndex(element => element.name == name);
                    if (index == -1) {
                        try {
                            avatar = assignee.avatarUrls["16x16"];
                        } catch (error) {
                            avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                        }
                        self.leaderboard.push(new Developer(avatar, name, 1));
                    } else {
                        self.leaderboard[index].issuecount++;
                    }
                });
                self.leaderboard.sort((a,b) => (a.issuecount > b.issuecount) ? -1 : ((b.issuecount > a.issuecount) ? 1 : 0))
                $element.empty().html(Leaderboard.Dashboard.Item.Templates.Leaderboard({leaderboard: self.leaderboard}));
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
    * REST call requesting all issues with status 'Done'
    */
    DashboardItem.prototype.requestData = function () {
        return $.ajax({
            method: "GET",
            url: contextPath() + "/rest/api/latest/search?jql=status%3Ddone"
        });
    };

    /**
    * Developer class for leaderbaord items
    */
    class Developer {
        constructor(avatar, name, issuecount) {
          this.avatar = avatar;
          this.name = name;
          this.issuecount = issuecount;
        };
      };

    return DashboardItem;
});
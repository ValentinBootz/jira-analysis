define('jira-dashboard-items/leaderboard', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

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
        Promise.all(this.requestDataPromises).then(function (data) {
            self.API.hideLoadingBar();
            self.issues = data[0].issues;

            /**
            * If response contains no issues use .Empty template
            */
            if (self.issues === undefined || self.issues.length  === 0) {
                $element.empty().html(Leaderboard.Dashboard.Item.Templates.Empty());
            }
            /**
            * Otherwise populate array with data for .Leaderboard template
            */
            else {
                self.leaderboard = [];
                self.issues.forEach(issue => {
                    /**
                    * Retrieve issue details by analyzing changelog and accumulate completed issues by developer
                    */
                    var developer = getDeveloper(issue.changelog);
                    name = developer ? developer.name : "Unspecified"
                    var index = self.leaderboard.findIndex(element => element.name == name);
                    if (index == -1) {
                        try {
                            avatar = developer.avatarUrls["16x16"];
                        } catch (error) {
                            avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                        }
                        var priorities = {highest: 0, high: 0, medium:0, low: 0, lowest: 0};
                        var types = {};
                        priorities[getPriority(issue).name.toLowerCase()]++;
                        self.leaderboard.push(new User({avatar, name, issues:1, priorities}));
                    } else {
                        self.leaderboard[index].issues++;
                        self.leaderboard[index].priorities[getPriority(issue).name.toLowerCase()]++;
                    }
                });
                self.leaderboard.sort((a,b) => (a.issues > b.issues) ? -1 : ((b.issues > a.issues) ? 1 : 0))
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
    * Promises for API calls requesting all issues with status 'Done' with expanded changelog and a list of issue priorities
    */
    DashboardItem.prototype.requestDataPromises = [
        "/rest/api/latest/search?jql=status%3Ddone&expand=changelog",
        "/rest/api/latest/priority"
        ].map(function(url){
            return $.ajax({
                method: "GET",
                url: contextPath() + url,
            });
        });

    /**
    * User class for leaderboard items
    */
    class User {
        constructor({avatar, name, issues, priorities} = {}) {
            this.avatar = avatar;
            this.name = name;
            this.issues = issues;
            this.priorities = priorities;
        };
    };

    /**
    * Gets developer from issue changelog. Credited is author who last changed status to 'In Progress'
    * 
    * @param changelog
    */
    function getDeveloper(changelog) {
        entry = changelog.histories.filter(
            function(histories){ return histories.items[0].toString == 'In Progress' }
        ).slice(-1)[0];
        return entry ? entry.author : undefined;
    }

    /**
    * Gets prioritiy from issue
    * 
    * @param issue
    */
    function getPriority(issue) {
        return issue.fields.priority;
    }

    return DashboardItem;
});
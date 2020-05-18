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
            * Otherwise populate array with data for .Leaderboard template
            */
            else {
                self.leaderboard = [];
                self.issues.forEach(issue => {
                    /**
                    * Accumulate initiated issues. Credited is issue creator
                    */
                    var creator = issue.fields.creator;
                    name = creator ? creator.name : "Unspecified"
                    var index = self.leaderboard.findIndex(element => element.name == name);
                    if (index == -1) {
                        try {
                            avatar = creator.avatarUrls["16x16"];
                        } catch (error) {
                            avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                        }
                        self.leaderboard.push(new User(avatar, name, initiated=1));
                    } else {
                        self.leaderboard[index].initiated++;
                    }
                    /**
                    * Accumulate completed issues. Credited is author who last changed status to 'In Progress'
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
                        self.leaderboard.push(new User(avatar, name, completed=1));
                    } else {
                        self.leaderboard[index].completed++;
                    }
                    /**
                    * Accumulate reviewed issues. Credited is author who last changed status to 'Approved'
                    */
                    var reviewer = getReviewer(issue.changelog);
                    name = reviewer ? reviewer.name : "Unspecified"
                    var index = self.leaderboard.findIndex(element => element.name == name);
                    if (index == -1) {
                        try {
                            avatar = reviewer.avatarUrls["16x16"];
                        } catch (error) {
                            avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                        }
                        self.leaderboard.push(new User(avatar, name, reviewed=1));
                    } else {
                        self.leaderboard[index].reviewed++;
                    }
                });
                self.leaderboard.sort((a,b) => (a.completed > b.completed) ? -1 : ((b.completed > a.completed) ? 1 : 0))
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
    * REST call requesting all issues with status 'Done' with expanded changelog
    */
    DashboardItem.prototype.requestData = function () {
        return $.ajax({
            method: "GET",
            url: contextPath() + "/rest/api/latest/search?jql=status%3Ddone&expand=changelog"
        });
    };

    /**
    * User class for leaderboard items
    */
    class User {
        constructor(avatar, name, initiated = 0, completed = 0, reviewed = 0) {
            this.avatar = avatar;
            this.name = name;
            this.initiated = initiated;
            this.completed = completed;
            this.reviewed = reviewed
        };
    };

    /**
    * Gets developer from issue changelog. Credited is the user who last changed status to 'In Progress'
    * 
    * @param changelog
    */
    function getDeveloper(changelog) {
        return changelog.histories.filter(
            function(histories){ return histories.items[0].toString == 'In Progress' }
        ).slice(-1)[0].author;
    }

    /**
    * Gets reviewer from issue changelog. Credited is the user who last changed status to 'Approved'
    * 
    * @param changelog
    */
    function getReviewer(changelog) {
        return changelog.histories.filter(
            function(histories){ return histories.items[0].toString == 'Approved' }
        ).slice(-1)[0].author;
    }

    return DashboardItem;
});
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
            self.priorities = data[1];
            self.types = data[2];
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
                /**
                * Initialize priorities template
                */
                const prioritiesTemplate = [];
                self.priorities.forEach(priority => {
                    prioritiesTemplate.push({name: priority.name, iconUrl: priority.iconUrl, count: 0});
                });
                /**
                * Initialize issue types template
                */
                const typesTemplate = [];
                self.types.forEach(type => {
                    typesTemplate.push({name: type.name, iconUrl: type.iconUrl, count: 0});
                });
                /**
                * Iterate issues and populate leaderboard
                */
                self.leaderboard = [];
                self.issues.forEach(issue => {
                    /**
                    * Retrieve issue details by analyzing changelog and accumulate completed issues by developer
                    */
                    var issuetype = getIssueType(issue);
                    var priority = getPriority(issue);
                    var estimated_time = getEstimatedTime(issue);
                    var developer = getDeveloper(issue.changelog);
                    name = developer ? developer.name : "Unspecified"
                    var index = self.leaderboard.findIndex(element => element.name == name);
                    /**
                    * If not on yet - add user to leaderboard
                    */
                    if (index == -1) {
                        try {
                            avatar = developer.avatarUrls["16x16"];
                        } catch (error) {
                            avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                        }
                        priorities = prioritiesTemplate;
                        priorities.find(element => element.name == priority.name).count++;
                        issuetypes = typesTemplate;
                        issuetypes.find(element => element.name == issuetype.name).count++;
                        self.leaderboard.push(new User({avatar, name, issues: 1, estimated_time, issuetypes, priorities}));
                    /**
                    * Otherwise update leaderboard data
                    */
                    } else {
                        self.leaderboard[index].issues++;
                        self.leaderboard[index].estimated_time += estimated_time;
                        self.leaderboard[index].priorities.find(element => element.name == priority.name).count++;
                        self.leaderboard[index].issuetypes.find(element => element.name == issuetype.name).count++;
                    }
                });
                self.leaderboard.forEach(function(element) {element.estimated_time = formatTimeEstimation(element.estimated_time)});
                /**
                * Sort leaderboard by completed issues 
                */
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
    * Promises for API calls requesting all issues with status 'Done' with expanded changelog and lists of issue priorities and types
    */
    DashboardItem.prototype.requestDataPromises = [
        "/rest/api/latest/search?jql=status%3Ddone&expand=changelog",
        "/rest/api/latest/priority",
        "/rest/api/latest/issuetype"
        ].map(function(url){
            return $.ajax({
                method: "GET",
                url: contextPath() + url
            });
        });

    /**
    * User class for leaderboard items
    */
    class User {
        constructor({avatar, name, issues, estimated_time, issuetypes, priorities} = {}) {
            this.avatar = avatar;
            this.name = name;
            this.issues = issues;
            this.estimated_time = estimated_time;
            this.issuetypes = issuetypes;
            this.priorities = priorities;
        };
    };

    /**
    * Formats time estimation (e.g. 1w 2d 5h 30m) to display readable format with standard workings days/weeks, hours and minutes
    * 
    * @param seconds
    */
    function formatTimeEstimation(timeoriginalestimate) {
        var res = "";
        var remaining = timeoriginalestimate;
        if (remaining >= 144000) {
            var weeks = Math.floor(timeoriginalestimate/144000);
            res += weeks + "w";
            remaining -= weeks*144000;
        }
        if (remaining >= 28800) {
            var days = Math.floor(remaining/28800);
            res += " " + days + "d";
            remaining -= days*28800;
        }
        if (remaining >= 3600) {
            var hours = Math.floor(remaining/3600);
            res += " " + hours + "h";
            remaining -= hours*3600;
        }
        if (remaining >= 60 || res == undefined) {
            var minutes = Math.floor(remaining/60);
            res += " " + minutes + "m";
        }
        return res.trim();
    }

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
    * Gets estimated time from issue
    * 
    * @param issue
    * @returns time in s or 0 if not specified
    */
    function getEstimatedTime(issue) {
        return issue.fields.timeoriginalestimate || 0; 
    }

    /**
    * Gets prioritiy from issue
    * 
    * @param issue
    */
    function getPriority(issue) {
        return issue.fields.priority;
    }

    /**
    * Gets issue type from issue
    * 
    * @param issue
    */
    function getIssueType(issue) {
        return issue.fields.issuetype;
    }

    return DashboardItem;
});
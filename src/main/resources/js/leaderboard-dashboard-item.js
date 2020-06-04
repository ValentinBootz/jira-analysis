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
            self.projects = data[3];
            self.users = data[4];
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
                * Iterate issues and populate user/project ranking
                */
                self.leaderboard_users = [];
                self.leaderboard_projects = [];
                self.issues.forEach(issue => {
                    /**
                    * Retrieve issue details
                    */
                    var issuetype = getIssueType(issue);
                    var priority = getPriority(issue);
                    var estimated_time = getEstimatedTime(issue);
                    /**
                    * Retrieve issue developer by analyzing changelog and accumulate completed issues by developer
                    */
                    var developer = getDeveloper(issue.changelog);
                    username = developer ? developer.name : "Unspecified";
                    var index = self.leaderboard_users.findIndex(element => element.name == username);
                    /**
                    * If not on yet - add user to user ranking
                    */
                    if (index == -1) {
                        try {
                            avatar = developer.avatarUrls["16x16"];
                        } catch (error) {
                            avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                        }
                        priorities = JSON.parse(JSON.stringify(prioritiesTemplate));
                        priorities.find(element => element.name == priority.name).count++;
                        issuetypes = JSON.parse(JSON.stringify(typesTemplate));
                        issuetypes.find(element => element.name == issuetype.name).count++;
                        self.leaderboard_users.push(new User({avatar, name: username, issues: 1, estimated_time, issuetypes, priorities}));
                    /**
                    * Otherwise update user ranking data
                    */
                    } else {
                        self.leaderboard_users[index].issues++;
                        self.leaderboard_users[index].estimated_time += estimated_time;
                        self.leaderboard_users[index].priorities.find(element => element.name == priority.name).count++;
                        self.leaderboard_users[index].issuetypes.find(element => element.name == issuetype.name).count++;
                    }
                    /**
                    * Retrieve project from issue and accumulate completed issues by project
                    */
                    var project = getProject(issue);
                    projectname = project ? project.name : "Unspecified";
                    var index = self.leaderboard_projects.findIndex(element => element.name == projectname);
                    /**
                    * If not on yet - add project to project ranking
                    */
                    if (index == -1) {
                        avatar = project.avatarUrls["16x16"];
                        priorities = JSON.parse(JSON.stringify(prioritiesTemplate));
                        priorities.find(element => element.name == priority.name).count++;
                        issuetypes = JSON.parse(JSON.stringify(typesTemplate));
                        issuetypes.find(element => element.name == issuetype.name).count++;
                        self.leaderboard_projects.push(new Project({avatar, name: projectname, issues: 1, estimated_time, issuetypes, priorities}));
                    /**
                    * Otherwise update project ranking data
                    */
                    } else {
                        self.leaderboard_projects[index].issues++;
                        self.leaderboard_projects[index].estimated_time += estimated_time;
                        self.leaderboard_projects[index].priorities.find(element => element.name == priority.name).count++;
                        self.leaderboard_projects[index].issuetypes.find(element => element.name == issuetype.name).count++;
                    }
                });
                self.leaderboard_users.forEach(function(element) {element.estimated_time = formatTimeEstimation(element.estimated_time)});
                self.leaderboard_projects.forEach(function(element) {element.estimated_time = formatTimeEstimation(element.estimated_time)});
                /**
                * Sort user/project ranking by completed issues 
                */
                self.leaderboard_users.sort((a,b) => (a.issues > b.issues) ? -1 : ((b.issues > a.issues) ? 1 : 0))
                self.leaderboard_projects.sort((a,b) => (a.issues > b.issues) ? -1 : ((b.issues > a.issues) ? 1 : 0))
                $element.empty().html(Leaderboard.Dashboard.Item.Templates.Leaderboard({leaderboard_users: self.leaderboard_users, leaderboard_projects: self.leaderboard_projects, priorities: self.priorities, types: self.types, projects: self.projects, users: self.users}));
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
        "/rest/api/latest/issuetype",
        "/rest/api/latest/project",
        "/rest/api/latest/user/search?username=''"
        ].map(function(url){
            return $.ajax({
                method: "GET",
                url: contextPath() + url
            });
        });

    /**
    * User class for user ranking items
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
    * Project class for project ranking items
    */
    class Project {
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
    * Gets project from issue
    * 
    * @param issue
    */
    function getProject(issue) {
        return issue.fields.project;
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
define('jira-dashboard-items/leaderboard', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

    var DashboardItem = function (API) {
        this.API = API;
    };

    /**
     * Called to render the view for leaderboard dashboard item.
     *
     * @param context The surrounding <div/> context that this item should render into.
     * @param preferences The user preferences saved for this dashboard item (e.g. filter id, number of results...)
     */
    DashboardItem.prototype.render = function (context, preferences) {
        this.API.showLoadingBar();
        var self = this;

        Promise.all(this.requestMetadata).then(function (data) {
            self.API.hideLoadingBar();
            loadFilters(self, context, data);
        });
    };

    /**
     * API calls requesting lists of projects, users, issue types and priorities
     */
    DashboardItem.prototype.requestMetadata = [
        "/rest/api/latest/project",
        "/rest/api/latest/user/search?username=''",
        "/rest/api/latest/issuetype",
        "/rest/api/latest/priority"
    ].map(function (url) {
        return $.ajax({
            method: "GET",
            url: contextPath() + url
        });
    });

    /**
     * Loads filter content dynamically and sets up event handler
     *
     * @param self
     * @param context
     * @param data
     */
    function loadFilters(self, context, data) {
        var $element = this.$element = $(context).find("#filters");
        [projects, users, types, priorities] = data;
        $element.empty().html(Dashboard.Plugin.Templates.Filters({
            projects: projects,
            users: users,
            types: types,
            priorities: priorities
        }));
        $element.find("#filter-form").on('submit', function (event) {
            event.preventDefault();
            initializeTemplates();
            handleSubmit(self, context)
        });
        self.API.resize();
    }

    /**
     * Submit filter form event handler.
     *
     * @param self the API context
     * @param context the HTML context
     */
    function handleSubmit(self, context) {
        self.API.showLoadingBar();

        requestAccess().done(function (grant) {
            self.API.hideLoadingBar();

            if (grant.granted) {
                // Access to data granted.
                self.API.showLoadingBar();
                requestData().done(function (response) {
                    self.API.hideLoadingBar();
                    data = analyzeProductivity(response.issues)
                    loadResults(self, context, data);
                });
            } else {
                // Access to data not granted.
                var $element = this.$element = $(context).find("#access-dialog");
                $element.empty().html(Dashboard.Plugin.Templates.AccessDialog());
                AJS.dialog2("#no-access-dialog").show();
            }
        });
    }

    /**
     * Requests access from the Logging and Access API.
     *
     * @returns {*} a grant with granted true or granted false
     */
    function requestAccess() {
        return $.ajax({
            type: "POST",
            url: contextPath() + "/rest/jira-analysis-api/1.0/logging-and-access/query/leaderboard",
            data: JSON.stringify($('#user-multiselect').val()),
            contentType: "application/json",
        });
    }

    /**
     * API call requesting all issues with status 'Done' with expanded changelog
     */
    function requestData() {
        jql_query = "jql=status%3Ddone";
        jql_query += $('#project-multiselect').val() ? encodeURIComponent(" AND project in (" + $('#project-multiselect').val().map(element => "\'" + element + "\'").join() + ")") : "";
        jql_query += $('#type-multiselect').val() ? encodeURIComponent(" AND issuetype in (" + $('#type-multiselect').val().map(element => "\'" + element + "\'").join() + ")") : "";
        jql_query += $('#priority-multiselect').val() ? encodeURIComponent(" AND priority in (" + $('#priority-multiselect').val().map(element => "\'" + element + "\'").join() + ")") : "";
        return $.ajax({
            method: "GET",
            url: contextPath() + "/rest/api/latest/search?" + jql_query + "&expand=changelog"
        });
    }

    /**
     * Analyze productivity for users/projects
     *
     * @param issues
     */
    function analyzeProductivity(issues) {
        data = { users: [], projects: [] };
        issues.forEach(issue => {
            details = getDetails(issue);
            if ($('#user-multiselect').val() ? $('#user-multiselect').val().includes(details.developer.name) : false) {
                updateUsers(data.users, details);
                updateProjects(data.projects, details);
            }
        });
        [data.users, data.projects].forEach(function (list) {
            list.forEach(element => element.estimate = formatEstimate(element.estimate));
            list.sort((a, b) => (a.count > b.count) ? -1 : ((b.count > a.count) ? 1 : 0));
            list.forEach(element => element.types = element.types.filter(function (type) {
                return type.count > 0;
            }));
            list.forEach(element => element.priorities = element.priorities.filter(function (type) {
                return type.count > 0;
            }));
        });
        return data;
    }

    /**
     * Update users in productivity data
     *
     * @param users
     * @param details
     */
    function updateUsers(users, details) {
        user = users.find(element => element.name == details.developer.name);
        if (user === undefined) {
            user = new Entity(details.developer);
            users.push(user);
        }

        user.count++;
        user.estimate += details.estimate;
        user.priorities.find(element => element.name == details.priority.name).count++;
        user.types.find(element => element.name == details.type.name).count++;
    }

    /**
     * Update projects in productivity data
     *
     * @param projects
     * @param details
     */
    function updateProjects(projects, details) {
        project = projects.find(element => element.name == details.project.name);
        if (project === undefined) {
            project = new Entity(details.project);
            projects.push(project);
        }

        project.count++;
        project.estimate += details.estimate;
        project.priorities.find(element => element.name == details.priority.name).count++;
        project.types.find(element => element.name == details.type.name).count++;
    }

    /**
     * Dynamically loads productivity data in results template
     *
     * @param data
     */
    function loadResults(self, context, data) {
        var $element = this.$element = $(context).find("#results");
        $element.empty().html(Leaderboard.Dashboard.Item.Templates.Results({
            users: data.users,
            projects: data.projects
        }));
        AJS.tabs.setup();
        self.API.resize();
    }

    /**
     *  Class for leaderboard entities
     */
    class Entity {
        constructor(entity) {
            this.avatar = entity.avatarUrls["16x16"];
            this.name = entity.name;
            this.count = 0;
            this.estimate = 0;
            this.types = JSON.parse(JSON.stringify(typesTemplate));
            this.priorities = JSON.parse(JSON.stringify(prioritiesTemplate));
        };
    }

    /**
     * Initialize types/priorities templates
     */
    function initializeTemplates() {
        prioritiesTemplate = [];
        priorities.forEach(priority => {
            prioritiesTemplate.push({ name: priority.name, iconUrl: priority.iconUrl, count: 0 });
        });
        typesTemplate = [];
        types.forEach(type => {
            typesTemplate.push({ name: type.name, iconUrl: type.iconUrl, count: 0 });
        });
    }

    /**
     * Formats time estimation (e.g. 1w 2d 5h 30m) to display readable format with standard workings days/weeks, hours and minutes
     *
     * @param seconds
     */
    function formatEstimate(timeoriginalestimate) {
        var res = "";
        var remaining = timeoriginalestimate;
        if (remaining >= 144000) {
            var weeks = Math.floor(timeoriginalestimate / 144000);
            res += weeks + "w";
            remaining -= weeks * 144000;
        }

        if (remaining >= 28800) {
            var days = Math.floor(remaining / 28800);
            res += " " + days + "d";
            remaining -= days * 28800;
        }

        if (remaining >= 3600) {
            var hours = Math.floor(remaining / 3600);
            res += " " + hours + "h";
            remaining -= hours * 3600;
        }

        if (remaining >= 60 || res == undefined) {
            var minutes = Math.floor(remaining / 60);
            res += " " + minutes + "m";
        }

        return res.trim();
    }

    /**
     * Retrieves issue details
     *
     * @param issue
     */
    function getDetails(issue) {
        return {
            project: getProject(issue),
            developer: getDeveloper(issue),
            estimate: getEstimatedTime(issue),
            type: getType(issue),
            priority: getPriority(issue)
        };
    }

    /**
     * Gets developer from issue. Credited is author who last changed status to 'In Progress'
     *
     * @param issue
     */
    function getDeveloper(issue) {
        entry = issue.changelog.histories.filter(
            function (histories) {
                return histories.items[0].toString == 'In Progress'
            }
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
     * @returns time estimate in s or 0 if not specified
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
    function getType(issue) {
        return issue.fields.issuetype;
    }

    return DashboardItem;
});
define('jira-dashboard-items/supporter', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

    var DashboardItem = function (API) {
        this.API = API;
    };

    /**
     * Called to render the view for a fully configured dashboard item.
     *
     * @param context The surrounding <div/> context that this item should render into
     * @param preferences The user preferences saved for this dashboard item (e.g. filter id, number of results...)
     */
    DashboardItem.prototype.render = function (context, preferences) {
        this.API.showLoadingBar();
        var self = this;

        Promise.all(this.requestMetadata).then(function (data) {
            self.API.hideLoadingBar();
            loadFilters(self, context, data);
        });

        this.API.once("afterRender", this.API.resize);
    }

    /**
     * Loads filter content dynamically and sets up event handler.
     *
     * @param self the API context
     * @param context the HTML context
     * @param data the data to be displayed in the filters
     */
    function loadFilters(self, context, data) {
        var $element = this.$element = $(context).find("#supporter-filters");
        [projects, users, types, priorities] = data;

        $element.empty().html(Dashboard.Plugin.Templates.Filters({
            type: 'supporter',
            projects: projects,
            users: users,
            types: types,
            priorities: priorities,
        }));

        $element.find("#supporter-filter-form").on('submit', function (event) {
            event.preventDefault();
            handleSubmit(self, context);
        });

        // Enable search once user is selected
        $(context).on("change", "#supporter-user-multiselect", function () {
            $("#supporter-filter").removeAttr("disabled");
        });

        // Toggle event handler
        $(context).on("change", "#supporter-filter-toggle", function () {
            $('#supporter-type-multiselect').children().removeProp('selected');
            $('#supporter-priority-multiselect').children().removeProp('selected');
            $("#supporter-optional-filter").toggle();
            self.API.resize();
        });

        self.API.resize();
    }

    /**
     * Submit filter form event handler and search for issues.
     *
     * @param self the API context
     * @param context the HTML context
     */
    function handleSubmit(self, context) {
        var $element = this.$element = $(context).find("#supporter-results");
        self.API.showLoadingBar();

        requestData().done(function (data) {
            self.API.hideLoadingBar();
            var developers = data;

            $element.empty().html(Supporter.Dashboard.Item.Templates.Results({ developers: developers }));

            self.API.resize();
        }).fail(function (jqXHR, textStatus, errorThrown) {
            self.API.hideLoadingBar();
            switch (jqXHR.status) {
                case 403:
                    // Display access not granted dialog.
                    var $element = this.$element = $(context).find("#supporter-access-dialog");
                    $element.empty().html(Dashboard.Plugin.Templates.AccessDialog({ type: 'supporter' }));
                    AJS.dialog2("#supporter-no-access-dialog").show();
                    break;
                default:
                    // Handle other errors.
                    window.alert(textStatus + ": " + errorThrown);
            }
        });
    }

    /**
     * Requests lists of projects, users, issue types and priorities for the filter.
     *
     * @type {*[]} lists of projects, users, issue types and priorities
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
     * Searches for open assigned issues.
     *
     * @returns {*} a list of open assigned issues
     */
    function requestData() {
        return $.ajax({
            type: "POST",
            url: contextPath() + "/rest/jira-analysis-api/1.0/supporter/reviewers",
            data: JSON.stringify({
                'users': $('#supporter-user-multiselect').val(),
                'projects': $('#supporter-project-multiselect').val(),
                'types': $('#supporter-type-multiselect').val(),
                'priorities': $('#supporter-priority-multiselect').val(),
            }),
            contentType: "application/json",
        });
    }

    return DashboardItem;
});
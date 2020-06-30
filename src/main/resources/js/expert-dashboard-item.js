define('jira-dashboard-items/expert', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

    var DashboardItem = function (API) {
        this.API = API;
        this.issues = [];
        this.searchword = "";
    };
    
    
    /**
     Called to render the view for a fully configured dashboard item.
     */
    DashboardItem.prototype.render = function (context, preferences) {
        var self = this;

        //Read in new keyword, to search for within issue titles, to find experts
        $(context).on('change','#search-text', function(){
            self.searchword = this.value;
            
            if(/\S/.test(self.searchword)){
                var $element = this.$element = $(context).find("#search");
                $element.removeAttr("disabled");
            }
            else{
                var $element = this.$element = $(context).find("#search");
                $element.attr("disabled", "disabled");
            }
        })
        
        //On-click event to start search
        $(context).on('click', '#search', function(){
            if(this.attributes.disabled === undefined){
                self.API.showLoadingBar();
                
                //Request Expert data (issue changelog with assignee name and issue status
                self.requestExperts().done(function (data) {
                    self.API.hideLoadingBar();
                    self.issues = data.issues;
                    
                    if(self.issues === undefined || self.issues.length  === 0){
                        var $element = this.$element = $(context).find("#expert-search-table");
                        $element.empty().html(Expert.Dashboard.Item.Templates.NoExperts());
                    }
                    else{
                        requestAccessAndCreateTable(self, context);
                    }
                    self.API.resize();
                });
            }
            else{
                var $element = this.$element = $(context).find("#expert-search-table");
                $element.empty().html(Expert.Dashboard.Item.Templates.EmptySearch());
                self.API.resize();
            }
        });
    };
    
    
    /**
     Get the names of all the developers in the issues
     */
    function getDeveloperNames(issues){
        var developerNames = [];
        issues.forEach(issue => {
            var developer = getDeveloper(issue.changelog);
            //... find the developers name, ...
            name = developer ? developer.name : "Unspecified"
            var index = developerNames.findIndex(element => element == name);
            if (index == -1) {
                developerNames.push(name);
            }
        });
        
        return developerNames;
    }
    
    
    /**
     Requests access to the data in case there are any issues with the keyword founs and creates a table of experts
     */
    function requestAccessAndCreateTable(self, context){
            var expertNames = getDeveloperNames(self.issues);

            //Request Access to the data
            requestAccess(expertNames).done(function (grant) {
                // Access to data granted.
                if (grant.granted) {
                    //Request expert data and create an expert table
                    createExpertTable(self.API, context, self.issues);
                    //Resize the window
                }
                // Access to data not granted.
                else {
                    var $element = this.$element = $(context).find("#expert-access-dialog");
                    $element.empty().html(Dashboard.Plugin.Templates.AccessDialog({ type: 'expert' }));
                    AJS.dialog2("#expert-no-access-dialog").show();
                }
            }).fail(function (jqXHR, textStatus, errorThrown) {
                switch (jqXHR.status) {
                    default:
                        // Handle errors.
                        window.alert(textStatus + ": " + errorThrown);
                }
            });
    }
    
    
    /**
     Create an expert table and set up the issue list table for each expert
     */
    function createExpertTable(API, context, expertIssues){
        //Call createExpertTable to create an array of all the experts and the issues they have worked on that include the search word
        var expertList = getExpertTable(expertIssues);
        //Sort expertList by the number of issues the expert worked on
        var sortedExpertList = expertList.sort((a,b) => (a.issues > b.issues) ? -1 : ((b.issues > a.issues) ? 1 : 0));
        
        //Display expert table (a table containing all the experts with the number of issues they have worked on
        var $element = this.$element = $(context).find("#expert-search-table");
        $element.empty().html(Expert.Dashboard.Item.Templates.ExpertTable({ content: expertList }));
        
        //Set up and display the issue sublists, a list of issues for each expert that is displayed when clicked on the expert
        setUpIssueLists(API, context, expertList);
    }
    
    
    /**
     Return an array of all the experts, including name and avatar of the expert, as well as the number of issues they have worked on and a list of the issues, with issue-id, issue-title and issue-summary
     */
    function getExpertTable(expertIssues){
        var expertAndIssueList = [];
        
        //For all issue entrys that include the search word, ...
        expertIssues.forEach(issue => {
            var developer = getDeveloper(issue.changelog);
            //... find the developers name, ...
            expert_name = developer ? developer.name : "Unspecified"
            var index = expertAndIssueList.findIndex(element => element.expert_name == expert_name);
            //... the issue ID, ...
            issue_id = issue.key;
            //... the issue title, ...
            issue_title = issue.fields.summary;
            //... the issue description ...
            if(issue.fields.description == null){
                issue_description = "";
            }
            else{
                issue_description = issue.fields.description;
            }
            //... a boolean parameter to check whether the issue subtable is displayed or not and ...
            issuetableOnDisplay = false;
            
            //... (as well as avatar and nummber of issues the expert worked on and that have the search work in the title) and create a list of experts and their issues
            if (index == -1) {
                try {
                    expert_avatar = developer.avatarUrls["16x16"];
                } catch (error) {
                    expert_avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                }
                expertAndIssueList.push(new User({expert_avatar, expert_name, issues: 1, issuetable: [new Issue({issue_id, issue_title, issue_description})], issuetableOnDisplay}));
            } else {
                expertAndIssueList[index].issues++;
                expertAndIssueList[index].issuetable.push(new Issue({issue_id, issue_title, issue_description}));
            }
        });
        return expertAndIssueList;
    }
    
    
    /**
     Create click on events for each expert teable entry, so the entry shows the issue list when clicked on
     */
    function setUpIssueLists(API, context, expertList){
        
        //For all experts, create ...
        for (var expertCount = 0; expertCount < expertList.length; expertCount++){
            //... a click on event, that ...
            $(context).on('click', '#' + expertList[expertCount].expert_name, function(){
                var expertName = this.children[0].innerText;
                var index = expertList.findIndex(element => element.expert_name == expertName);
                
                //...A. if the issue subtable is already displayed, shows the original (only experts and issue count is displayed, no list of issues) table ...
                if(expertList[index].issuetableOnDisplay){
                    var $element = this.$element = $(context).find("#" + expertName + "-issues");
                    $element.empty().html(expertList[index].issues);
                    
                    //issuetableOnDisplay_b is a boolean parameter, that is saved in our expert  list and used to check whether the subtable (List of issues) is displayed or not
                    expertList[index].issuetableOnDisplay = false;
                }
                //...B. Or, if the original (only experts and issue count is displayed, no list of issues) table has been displayed before, shows the issue subtable
                else{
                    var issueTable = expertList[index].issuetable;
                    var sortedIssueTable = issueTable.sort((a,b) => (a.issue_id > b.issue_id) ? -1 : ((b.issue_id > a.issue_id) ? 1 : 0));
                    var $element = this.$element = $(context).find("#" + expertName + "-issues");
                    $element.empty().html(Expert.Dashboard.Item.Templates.IssueTable({ path: contextPath() + '/browse/', content: issueTable }));
                    
                    //issuetableOnDisplay_b is a boolean parameter, that is saved in our expert  list and used to check whether the subtable (List of issues) is displayed or not
                    expertList[index].issuetableOnDisplay = true;
                }
                
                //Resize the window
                API.resize();
            })
        }
    }
    
    
    /**
    Get the developer of an issue, aka the last user that set the item to "In Progress"
    */
    function getDeveloper(changelog) {
        entry = changelog.histories.filter(
            function(histories){
                return histories.items[0] !== undefined && histories.items[0].toString === 'In Progress'
        }
        ).slice(-1)[0];
        return entry ? entry.author : undefined;
    }
    
    /**
     * Requests access from the Logging and Access API.
     *
     * @returns {*} a grant with granted true or granted false
     */
    function requestAccess(expertNameList) {
        return $.ajax({
            type: "POST",
            url: contextPath() + "/rest/jira-analysis-api/1.0/logging-and-access/query/expert",
            data: JSON.stringify(expertNameList),
            contentType: "application/json",
        });
    }
    
    
    /**
    REST call requesting all issues with status 'Done' with expanded changelog
    */
    DashboardItem.prototype.requestExperts = function () {
        return $.ajax({
            method: "GET",
        url: contextPath() + "/rest/api/latest/search?jql=status%20%3D%20Done%20AND%20text%20~%20%22"+ this.searchword + "%22&expand=changelog&fields=id,key,status,summary,description"
        });
    };
    
    
    /**
     Class to greate a user object that is later used for the expert table
     */
    class User {
        constructor({expert_avatar, expert_name, issues, issuetable, issuetableOnDisplay} = {}) {
            this.expert_avatar = expert_avatar;
            this.expert_name = expert_name;
            this.issues = issues;
            this.issuetable = issuetable;
            this.issuetableOnDisplay = issuetableOnDisplay;
        };
    };
    
    
    /**
     Class to greate an issue object that is later used for the extended expert table
     */
    class Issue {
        constructor({issue_id, issue_title, issue_description} = {}) {
            this.issue_id = issue_id;
            this.issue_title = issue_title;
            this.issue_description = issue_description;
        };
    };

    
    return DashboardItem;
});

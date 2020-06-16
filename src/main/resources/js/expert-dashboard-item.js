define('jira-dashboard-items/expert', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

    var DashboardItem = function (API) {
        this.API = API;
        this.issues = [];
    };
    
    /**
     Called to render the view for a fully configured dashboard item.
     */
    DashboardItem.prototype.render = function (context, preferences) {
        this.API.showLoadingBar();
        var self = this;
        //Keyword, to search for within issue titles, to find experts
        var searchword = "";

        
        //Readin new searchword
        $(context).on('change','#search-text', function(){
            searchword = this.value;
        })
        
        
        
        requestAccess().done(function (grant) {
            // Access to data granted.
            if (grant.granted) {
                //Get issue data and set up expert-search
                this.requestData().done(function (data) {
                    self.API.hideLoadingBar();
                    self.issues = data.issues;
                    
                    //On-click event to start search
                    $(context).on('click', '#search', function(){
                        
                        //If there are no issues found, give out a warning TODO: Make this and next if into one
                        if(self.issues === undefined || self.issues.length  === 0){
                            $(context).find('#expert-table').html("So far there are no finished dasks. Please finish tasks before you search for experts.");
                        }
                        //Only use expert-search if there are "done" issues
                        else{
                            //Filter experts TODO: this should be unnecessary
                            var expertIssues = filterIssuesWithKeyword(self.issues, searchword);
                            
                            //If there are no experts, give out a warning ...
                            if(expertIssues.length === 0 || expertIssues === undefined){
                                $(context).find('#expert-table').html("No experts for " + searchword + " found.");
                            }
                            //... otherwise set up the expert table
                            else {
                                setUpExpertTable(self.API, context, expertIssues);
                            }
                       }
                        
                        //Resize the window
                        self.API.resize();
                    });
                });
            // Access to data not granted.
            } else {
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
    };
    
    
    /**
     */
    function setUpExpertTable(API, context, expertIssues){
        //Call createExpertTable to create an array of all the experts and the issues they have worked on that include the search word
        var expertList = createExpertTable(expertIssues);
        //Sort expertList by the number of issues the expert worked on
        var sortedExpertList = expertList.sort((a,b) => (a.issues > b.issues) ? -1 : ((b.issues > a.issues) ? 1 : 0));
        
        //Display expert table (a table containing all the experts with the number of issues they have worked on
        $(context).find('#expert-table').html(writeExpertTable(context, expertList));
        
        //Set up and display the issue sublists, a list of issues for each expert that is displayed when clicked on the expert
        setUpIssueLists(API, context, expertList);
    }
    
    
    /**
     Create an array of all the experts, including name and avatar of the expert, as well as the number of issues they have worked on and a list of the issues, with issue-id, issue-title and issue-summary
     */
    function createExpertTable(expertIssues){
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
            issue_description = issue.fields.summary;
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
     
     */
    function setUpIssueLists(API, context, expertList){
        //For all experts, create ...
        for (var expertCount = 0; expertCount < expertList.length; expertCount++){
            //... a click on event, that
            $(context).on('click', '#' + expertCount, function(){
                var expertName = this.children[0].innerText;
                var index = expertList.findIndex(element => element.expert_name == expertName);
                
                //... if the issue subtable is already displayed, shows the original (only experts and issue count is displayed, no list of issues) table ...
                if(expertList[index].issuetableOnDisplay){
                    //TODO: make this in a nice way
                    $(context).find('#expert-table').html(writeExpertTable(expertList));
                    
                    //issuetableOnDisplay_b is a boolean parameter, that is saved in our expert  list and used to check whether the subtable (List of issues) is displayed or not
                    expertList[index].issuetableOnDisplay = false;
                }
                //... Or, if the original (only experts and issue count is displayed, no list of issues) table has been displayed before, shows the issue subtable
                else{
                    var issueTable = expertList[index].issuetable;
                    
                    //TODO: make this in a nice way
                    this.children[1].innerHTML = writeIssueSubTable(issueTable);
                    
                    //issuetableOnDisplay_b is a boolean parameter, that is saved in our expert  list and used to check whether the subtable (List of issues) is displayed or not
                    expertList[index].issuetableOnDisplay = true;
                }
                
                //Resize the window
                API.resize();
            })
        }
    }
    
    
    /**
     Creates a table with the names of the experts and the nummber of issues they solved.
        Next step: Improve so Table is not written as HTML String
     */
    function writeExpertTable(context, expertList){
        var table = "<thead><tr><th id=" + "name"+ ">User</th><th id=" +"issues"+ ">Completed Issues</th></tr></thead><tbody>";
        
        var countExpert = 0;
        expertList.forEach(expert => {
            table = table + "<tr id = " + countExpert + "><td headers=" + "name" + "><span class=" + "container" + "><span class=" + "aui-avatar aui-avatar-xsmall" + "><span class=" + "aui-avatar-inner" + "><img src=" + expert.expert_avatar +  " alt=" +" role=" + "presentation"+ "/></span></span>" + expert.expert_name + "</span></td><td headers=" + "issues" + ">" + expert.issues + "</td></tr>";
            
            countExpert++;
        });
        table = table + "</tbody>";
        $(table).appendTo('#expert-table');
    }
    
    /**
     Creates a table with the issue id and summary for the specific expert
        Next step: Improve so Table is not written as HTML String
     */
    function writeIssueSubTable(issueTable){
        var table = "<td><table id=" + "issue-table" + " class=" + "aui aui-table-sortable" + "><thead><tr><th id=" + "issue"+ ">Issue</th><th id=" +"summary"+ ">Summary</th></tr></thead><tbody>";
        
        issueTable.forEach(issue => {
            table = table + "<tr><td headers=" + "issue" + "><a href=" + contextPath()+ "/browse/"+ issue.issue_id + ">" + issue.issue_id  +"</a></td><td headers=" + "summary" + ">" + issue.issue_title + "</td></tr>";
        });
        
        return table + "</tbody></table></td>";
    }
    
    
    /**
     Filters the issues that have the keyword in their title
     */
    function filterIssuesWithKeyword(unfilteredIssues, keyword){
        var expertIssues = [];
        for(fi = 0; fi < unfilteredIssues.length; fi++){
            if (unfilteredIssues[fi].fields.summary.includes(keyword)){
                expertIssues.push(unfilteredIssues[fi]);
            }
        }
        return expertIssues;
    }
    
    
    /**
    Get the developer of an issue, aka the last user that set the item to "In Progress"
    */
    function getDeveloper(changelog) {
        entry = changelog.histories.filter(
            function(histories){ return histories.items[0].toString == 'In Progress' }
        ).slice(-1)[0];
        return entry ? entry.author : undefined;
    }
    
    /**
     * Requests access from the Logging and Access API.
     *
     * @returns {*} a grant with granted true or granted false
     */
    function requestAccess() {
        return $.ajax({
            type: "POST",
            url: contextPath() + "/rest/jira-analysis-api/1.0/logging-and-access/query/expert",
            data: JSON.stringify(["example_username"]),
            contentType: "application/json",
        });
    }
    
    /**
    REST call requesting all issues with status 'Done' with expanded changelog
    */
    DashboardItem.prototype.requestData = function () {
        return $.ajax({
            method: "GET",
            url: contextPath() + "/rest/api/latest/search?jql=status%3Ddone&expand=changelog"
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

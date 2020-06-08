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
        var $element = this.$element = $(context);
        var self = this;
        var keyword = "";
        var expert = [];

        /**
         Event to get our keyword
         */
        $(context).on('change','#search-text', function(){
            keyword = this.value;
        })
        
        /**
         Request issues that have been done and create the expert table
         */
        this.requestData().done(function (data) {
            self.API.hideLoadingBar();
            self.issues = data.issues;
            
            /**
             Click search button, to start search ...
             */
            $(context).on('click', '#search', function(){
                //Delete previous table
                $(context).find('#expert-table').html("");
                
                /**
                 If there are no done tasks, give a warning
                 */
                if(self.issues === undefined || self.issues.length  === 0){
                    $(context).find('#expert-table').html("So far there are no finished dasks. Please finish tasks before you search for experts.");
                }
                /**
                 Only use expert search if there are actually done items
                 */
                else{
                    /**
                     Filter experts
                     */
                    var expertIssues = filterIssuesWithKeyword(self.issues, keyword);
                    
                    /**
                     If there are no experts, give out a message
                     */
                    if(expertIssues.length === 0 || expertIssues === undefined){
                        $(context).find('#expert-table').html("No experts for " + keyword + " found.");
                    }
                    /**
                     If there are experts, create a table
                     */
                    else {
                        /**
                         Call createExpertTable to create an array with all the experts
                         */
                        expert = createExpertTable(expertIssues);
                        
                        /**
                         Order entrences by issue count
                         */
                        expert.sort((a,b) => (a.issues > b.issues) ? -1 : ((b.issues > a.issues) ? 1 : 0));
                        
                        //Write table
                        $(context).find('#expert-table').html(writeExpertTable(expert));
                        
                        /**
                         Make click on event for all table entries
                         */
                        var isSubTableOpen = false;
                        for (var expcount = 0; expcount < expert.length; expcount++){
                            $(context).on('click', '#' + expcount, function(){
                                if(isSubTableOpen){
                                    $(context).find('#expert-table').html(writeExpertTable(expert));
                                    isSubTableOpen = false;
                                }
                                else{
                                    var expertName = this.children[0].innerText;
                                    
                                    var issueTable = createIssueSubTable(expertName, expertIssues);
                                    
                                    this.children[1].innerHTML = writeIssueSubTable(issueTable);
                                    
                                    isSubTableOpen = true;
                                }
                                
                                /**
                                 Resize the window
                                 */
                                self.API.resize();
                            })
                        }
                    }
               }
                
                /**
                 Resize the window
                 */
                self.API.resize();
            });
        });
    };
    
    
    /**
     Create an array of all the experts
     */
    function createExpertTable(eIssues){
        var e = [];
        
        eIssues.forEach(issue => {
            var developer = getDeveloper(issue.changelog);
            name = developer ? developer.name : "Unspecified"
            var index = e.findIndex(element => element.name == name);
            
            if (index == -1) {
                try {
                    avatar = developer.avatarUrls["16x16"];
                } catch (error) {
                    avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                }
                e.push(new User({avatar, name, issues: 1}));
                
            } else {
                e[index].issues++;
            }
        });
        return e;
    }
    
    /**
     Create an array of all the issues the expert has worked on, that also include the keyword
     */
    function createIssueSubTable(expertN, eIssues){
        var issueTable = [];
        
        eIssues.forEach(issue => {
            var developer = getDeveloper(issue.changelog);
            name = developer ? developer.name : "Unspecified"
            if(developer.name === expertN){
                key = issue.key;
                link = issue.self;
                summary = issue.fields.summary;
                issueTable.push(new Issue({key, link, summary}));
            }
        });
        return issueTable;
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
     Class to greate a user object that is later used for the expert table
     */
    class User {
        constructor({avatar, name, issues} = {}) {
            this.avatar = avatar;
            this.name = name;
            this.issues = issues;
        };
    };
    
    /**
     Class to greate an issue object that is later used for the extended expert table
     */
    class Issue {
        constructor({key, link, summary} = {}) {
            this.key = key;
            this.link = link;
            this.summary = summary;
        };
    };
    
    
    /**
     Creates a table with the names of the experts and the nummber of issues they solved.
     */
    function writeExpertTable(experts){
        var table = "<thead><tr><th id=" + "name"+ ">User</th><th id=" +"issues"+ ">Completed Issues</th></tr></thead><tbody>";
        
        var countE = 0;
        experts.forEach(expert => {
            table = table + "<tr id = " + countE + "><td headers=" + "name" + "><span class=" + "container" + "><span class=" + "aui-avatar aui-avatar-xsmall" + "><span class=" + "aui-avatar-inner" + "><img src=" + expert.avatar +  " alt=" +" role=" + "presentation"+ "/></span></span>" + expert.name + "</span></td><td headers=" + "issues" + ">" + expert.issues + "</td></tr>";
            
            //<table id=" + "issue-table" + countE + " class=" + "aui aui-table-sortable" + "></table>
            
            countE++;
        });
        return table + "</tbody>";
    }
    
    /**
     Creates a table with the issue id and summary for the specific expert
     */
    function writeIssueSubTable(issueTable){
        var table = "<td><table id=" + "issue-table" + " class=" + "aui aui-table-sortable" + "><thead><tr><th id=" + "issue"+ ">Issue</th><th id=" +"summary"+ ">Summary</th></tr></thead><tbody>";
        
        issueTable.forEach(issue => {
            table = table + "<tr><td headers=" + "issue" + "><a href=" + issue.link + ">" + issue.key  +"</a></td><td headers=" + "summary" + ">" + issue.summary + "</td></tr>";
        });
        
        return table + "</tbody></table></td>";
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

    
    return DashboardItem;
});

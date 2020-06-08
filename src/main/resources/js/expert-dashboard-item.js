define('jira-dashboard-items/expert', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

    var DashboardItem = function (API) {
        this.API = API;
        this.issues = [];
    };
    
    /**
     Called to render the view for a fully configured dashboard item.
        Next Step: Cut this down in smaller functions
     */
    DashboardItem.prototype.render = function (context, preferences) {
        this.API.showLoadingBar();
        var self = this;
        
        var keyword = "";
        var expertList = [];

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
                    var keywordIssues = filterIssuesWithKeyword(self.issues, keyword);
                    
                    /**
                     If there are no experts, give out a message
                     */
                    if(keywordIssues.length === 0 || keywordIssues === undefined){
                        $(context).find('#expert-table').html("No experts for " + keyword + " found.");
                    }
                    /**
                     If there are experts, create a table
                     */
                    else {
                        /**
                         Call createExpertTable to create an array with all the experts
                         */
                        expertList = createExpertTable(keywordIssues);
                        
                        /**
                         Order entrences by issue count
                         */
                        expertList.sort((a,b) => (a.issues > b.issues) ? -1 : ((b.issues > a.issues) ? 1 : 0));
                        
                        /**
                         Write expert table
                         */
                        $(context).find('#expert-table').html(writeExpertTable(expertList));
                        
                        /**
                         For all Table entries ...
                         */
                        var isSubTableOpen = false;
                        for (var expertCount = 0; expertCount < expertList.length; expertCount++){
                            /**
                             ... when clicked on, display a able of all the issues that have the keyword in them
                             */
                            $(context).on('click', '#' + expertCount, function(){
                                /**
                                 If the Issue table is shown, create table without issues
                                 */
                                if(isSubTableOpen){
                                    $(context).find('#expert-table').html(writeExpertTable(expertList));
                                    isSubTableOpen = false;
                                }
                                /**
                                 If Issues are not shown, show issues
                                 */
                                else{
                                    var expertName = this.children[0].innerText;
                                    var issueTable = createIssueSubTable(expertName, keywordIssues);
                                    
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
    function createExpertTable(filteredExpertIssues){
        var experts = [];
        
        filteredExpertIssues.forEach(issue => {
            var developer = getDeveloper(issue.changelog);
            name = developer ? developer.name : "Unspecified"
            var index = experts.findIndex(element => element.name == name);
            
            if (index == -1) {
                try {
                    avatar = developer.avatarUrls["16x16"];
                } catch (error) {
                    avatar = "/jira/secure/useravatar?size=xsmall&avatarId=10123";
                }
                experts.push(new User({avatar, name, issues: 1}));
                
            } else {
                experts[index].issues++;
            }
        });
        
        return experts;
    }
    
    
    /**
     Create an array of all the issues the expert has worked on, that also include the keyword
     */
    function createIssueSubTable(expertName, expertIssues){
        var issueTable = [];
        
        expertIssues.forEach(issue => {
            var developer = getDeveloper(issue.changelog);
            name = developer ? developer.name : "Unspecified"
            
            if(developer.name === expertName){
                key = issue.key;
                summary = issue.fields.summary;
                issueTable.push(new Issue({key, summary}));
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
     Creates a table with the names of the experts and the nummber of issues they solved.
        Next step: Improve so Table is not written as HTML String
     */
    function writeExpertTable(expertList){
        var table = "<thead><tr><th id=" + "name"+ ">User</th><th id=" +"issues"+ ">Completed Issues</th></tr></thead><tbody>";
        
        var countExpert = 0;
        expertList.forEach(expert => {
            table = table + "<tr id = " + countExpert + "><td headers=" + "name" + "><span class=" + "container" + "><span class=" + "aui-avatar aui-avatar-xsmall" + "><span class=" + "aui-avatar-inner" + "><img src=" + expert.avatar +  " alt=" +" role=" + "presentation"+ "/></span></span>" + expert.name + "</span></td><td headers=" + "issues" + ">" + expert.issues + "</td></tr>";
            
            countExpert++;
        });
        return table + "</tbody>";
    }
    
    /**
     Creates a table with the issue id and summary for the specific expert
        Next step: Improve so Table is not written as HTML String
     */
    function writeIssueSubTable(issueTable){
        var table = "<td><table id=" + "issue-table" + " class=" + "aui aui-table-sortable" + "><thead><tr><th id=" + "issue"+ ">Issue</th><th id=" +"summary"+ ">Summary</th></tr></thead><tbody>";
        
        issueTable.forEach(issue => {
            table = table + "<tr><td headers=" + "issue" + "><a href=" + contextPath()+ "/browse/"+ issue.key + ">" + issue.key  +"</a></td><td headers=" + "summary" + ">" + issue.summary + "</td></tr>";
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
        constructor({key, summary} = {}) {
            this.key = key;
            this.summary = summary;
        };
    };

    
    
    return DashboardItem;
});

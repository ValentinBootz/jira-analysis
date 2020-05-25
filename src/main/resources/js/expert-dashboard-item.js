define('jira-dashboard-items/expert', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {

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
        var $element = this.$element = $(context);
        var self = this;
        var expertTable = [];
        var expertTableNumber = [];
        var keyword = "";
        
        $(context).on('change','#search-text', function(){
            keyword = this.value;
        })
        this.requestData().done(function (data) {
            self.API.hideLoadingBar();
            self.issues = data.issues;
            

            if(self.issues === undefined || self.issues.length  === 0){
                //do nothing for now
            }
            else{
                $(context).on('click', '#search', function(){
                    
                    //filter issues
                    var expertIssues = filterIssuesWithKeyword(self.issues, keyword);
                    
                    //create array for table with names and nummbers of keyword issues
                    if(expertIssues === undefined || expertIssues.length  === 0){
                    }
                    else{
                        for(ei = 0; ei < expertIssues.length; ei++){
                            if(expertIssues[ei].changelog === undefined){
                            }else{
                                var developer = getDeveloper(expertIssues[ei].changelog);
                                if(developer === undefined){
                                }else{
                                    var index = expertTable.indexOf(developer);
                                    if(expertTable.length === 0 || expertTable === undefined ){
                                        expertTable.push(developer);
                                        expertTableNumber.push(1);
                                    }else{
                                        expertTableNumber[index]++;
                                    }
                                }
                            }
                        }
                    }
                    
                    //create the table so we can give it out in html
                    var bool = $(context).find('#test-table').innerHTML = "Hello";
                    
                    var x = "test";
                });
            }
        });

        this.API.once("afterRender", this.API.resize);
    };
    
    
    /**
     Filters the issues that have the keyword in their title
     */
    function filterIssuesWithKeyword(unfilteredIssues, keyword){
        var expertIssues = [];
        for(fi = 0; fi < unfilteredIssues.length; fi++){
            var x =unfilteredIssues[fi].fields.summary.includes(keyword);
            if (unfilteredIssues[fi].fields.summary.includes(keyword)){
                expertIssues.push(unfilteredIssues[fi]);
            }
        }
        return expertIssues;
    }
    
    /**
    * Gets developer from issue changelog. Credited is author who last changed status to 'In Progress'
    *
    * @param changelog
    */
    function getDeveloper(changelog) {
        var developers = [];
        
        for(i = 0; i < changelog.histories.length; i++){
            if(changelog.histories[i].items[0].toString === "In Progress"){
                developers.push(changelog.histories[i].author.name);
            }
        }
        return developers[developers.length-1];
    }
    
    /**
     Creates a table with the names of the experts and the nummber of issues they solved.
     */
    function createExpertTable(expertName, expertNummber){
        if(expertNummber === undefined  || expertName === undefined){
            return "No Experts found";
        }
        else{
            //var sortedNumb = expertNummber;
             //sortedNumb.sort();
             var tabletext = "<table class=" + "aui" + "><thead><tr><th id=" + "name" + ">Name</th><th id=" + "number" + ">Ranking</th></tr></thead><tbody>";
             
            for(ti = 0; ti < expertNummber.length; ti++){
                 tabletext = tabletext + "<tr><td headers=" + "name" +">" + expertNummber[ti] +"</td><td headers="+ "number" +">" + expertName[ti] + "</td></tr>";
             }
             return tabletext + "</tbody></table>";
        }
    }

    /**
    * REST call requesting all issues with status 'Done' with expanded changelog
    */
    DashboardItem.prototype.requestData = function () {
        return $.ajax({
            method: "GET",
            url: contextPath() + "/rest/api/latest/search?jql=status%3Ddone&expand=changelog"
        });
    };

    
    return DashboardItem;
});

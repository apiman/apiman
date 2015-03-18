/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var ConsumerOrgsController = _module.controller("Apiman.ConsumerOrgsController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $location, $scope, ApimanSvcs, PageLifecycle) => {

            $scope.orgs = [];
            
            $scope.searchOrg = function() { 
                var body:any = {};
                body.filters = [];
                body.filters.push( {"name": "name", "value": $scope.orgName + "%", "operator": "like"});
                var searchStr = JSON.stringify(body);
                
                ApimanSvcs.save({ entityType: 'search', secondaryType: 'organizations' }, searchStr, function(reply) {
                    $scope.orgs = reply.beans;
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            PageLifecycle.loadPage('ConsumerOrgs', undefined, $scope);
        }]);

}

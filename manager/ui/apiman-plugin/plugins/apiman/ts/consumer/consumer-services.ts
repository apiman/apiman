/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var ConsumerSvcsController = _module.controller("Apiman.ConsumerSvcsController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $location, $scope, ApimanSvcs, PageLifecycle) => {

            $scope.services = [];
            
            $scope.searchSvcs = function() { 
                var body:any = {};
                body.filters = [];
                body.filters.push( {"name": "name", "value": $scope.serviceName + "%", "operator": "like"});
                var searchStr = JSON.stringify(body);
                
                ApimanSvcs.save({ entityType: 'search', secondaryType: 'services' }, searchStr, function(reply) {
                    $scope.services = reply.beans;
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            PageLifecycle.loadPage('ConsumerSvcs', undefined, $scope);
        }]);

}

/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var ConsumerSvcsController = _module.controller("Apiman.ConsumerSvcsController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            if (params.q) {
                $scope.serviceName = params.q;
            }

            $scope.searchSvcs = function(value) {
                $location.search('q', value);
            };
            
            var pageData = {
                services: $q(function(resolve, reject) {
                    if (params.q) {
                        var body:any = {};
                        body.filters = [];
                        body.filters.push( {"name": "name", "value": "%" + params.q + "%", "operator": "like"});
                        var searchStr = angular.toJson(body);
                        
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'services' }, searchStr, function(reply) {
                            resolve(reply.beans);
                        }, reject);
                    } else {
                        resolve([]);
                    }
                })
            };

            PageLifecycle.loadPage('ConsumerSvcs', pageData, $scope, function() {
                PageLifecycle.setPageTitle('consumer-services');
                $scope.$applyAsync(function() {
                    $('#apiman-search').focus();
                });
            });
        }]);

}

/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var ConsumerApisController = _module.controller("Apiman.ConsumerApisController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            if (params.q) {
                $scope.apiName = params.q;
            }

            $scope.searchSvcs = function(value, currentPage, pageSize) {
                $location.search({
                    'q': value,
                    'cp': currentPage,
                    'ps': pageSize
                });
            };
            
            var pageData = {
                apis: $q(function(resolve, reject) {
                    if (params.q && params.cp && params.ps) {
                        var body:any = {};
                        body.filters = [];

                        body.page = params.cp
                        body.pageSize = params.ps

                        body.filters.push( {"name": "name", "value": "*" + params.q + "*", "operator": "like"});
                        var searchStr = angular.toJson(body);
                        
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'apis' }, searchStr, function(reply) {
                            
                            $scope.resultCount = reply.totalSize;
                            $scope.currentPage = params.cp;
                            $scope.pageSize = params.ps

                            resolve(reply.beans);
                        }, reject);
                    } else {
                        resolve([]);
                    }
                })
            };

            PageLifecycle.loadPage('ConsumerApis', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('consumer-apis');
                $scope.$applyAsync(function() {
                    $('#apiman-search').focus();
                });
            });
        }]);

}

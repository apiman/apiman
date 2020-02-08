/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var ConsumerOrgsController = _module.controller("Apiman.ConsumerOrgsController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger', 'CurrentUser',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger, CurrentUser) => {
            var params = $location.search();
            if (params.q) {
                $scope.orgName = params.q;
            }
            
            $scope.pageSize = 10;    //default page size
            $scope.currentPage = 1; //initial page index

            $scope.searchOrg = function(value) {
                $location.search('q', value);
            };
            
            var pageData = {
                orgs: $q(function(resolve, reject) {
                    if (params.q) {
                        var body:any = {};
                        body.filters = [];
                        body.filters.push( {"name": "name", "value": "*" + params.q + "*", "operator": "like"});
                        body.page = 1;          //default on backend for some functions
                        body.pageSize = 500;    //default on backend for some functions

                        var searchStr = angular.toJson(body);
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'organizations' }, searchStr, function(result) { 
                            resolve(result.beans);
                        }, reject);
                    } else {
                        resolve([]);
            
                    }
                })
            };
            
            PageLifecycle.loadPage('ConsumerOrgs', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('consumer-orgs');
                $scope.$applyAsync(function() {
                    angular.forEach($scope.orgs, function(org) {
                        org.isMember = CurrentUser.isMember(org.id);
                    });
                    $('#apiman-search').focus();
                });
            });
        }]);

}

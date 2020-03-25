/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var ConsumerOrgsController = _module.controller("Apiman.ConsumerOrgsController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger', 'CurrentUser',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger, CurrentUser) => {
            var params = $location.search();

            if (params.q) {
                $scope.orgName = params.q;
            }

            $scope.searchOrg = function(value, currentPage, pageSize) {
                $location.search({
                    'q': value,
                    'cp': currentPage,
                    'ps': pageSize
                });
            };

            var pageData = {
                orgs: $q(function(resolve, reject) {
                    if (params.q && params.cp && params.ps) {
                        var body:any = {};
                        body.filters = [];

                        body.page = params.cp;
                        body.pageSize = params.ps;

                        body.filters.push( {"name": "name", "value": "*" + params.q + "*", "operator": "like"});
                        var searchStr = angular.toJson(body);
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'organizations' }, searchStr, function(result) { 

                            $scope.resultCount = result.totalSize;
                            $scope.currentPage = params.cp;
                            $scope.pageSize = params.ps;

                            resolve(result.beans);
                        }, reject);
                    } else {
                        resolve([]);
                    }
                })
            };

            function loadFirstPage() {
                if ($scope.orgs.length == 0) {
                    $scope.searchOrg('*', 1, 10);
                }
            }

            PageLifecycle.loadPage('ConsumerOrgs', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('consumer-orgs');
                loadFirstPage();
                $scope.$applyAsync(function() {
                    angular.forEach($scope.orgs, function(org) {
                        org.isMember = CurrentUser.isMember(org.id);
                    });
                    $('#apiman-search').val('').focus();
                });
            });
        }]);

}

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

 export var ApiActivityController = _module.controller("Apiman.ApiActivityController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'AuditSvcs', '$routeParams', 'Configuration',
         ($q, $scope, $location, PageLifecycle, ApiEntityLoader, AuditSvcs, $routeParams, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;
            
            var getNextPage = function(successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;

                AuditSvcs.get({ organizationId: params.org, entityType: 'apis', entityId: params.api, page: $scope.currentPage, count: 20 }, function(results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };
             
            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            pageData = angular.extend(pageData, {
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            
            $scope.getNextPage = getNextPage;

            PageLifecycle.loadPage('ApiActivity', 'apiView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('api-activity', [ $scope.api.name ]);
            });
        }])

}

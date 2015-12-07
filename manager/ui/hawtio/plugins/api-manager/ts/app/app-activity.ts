/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var AppActivityController = _module.controller("Apiman.AppActivityController",
        ['$q', '$scope', '$location', 'Logger', 'PageLifecycle', 'AppEntityLoader', 'AuditSvcs', '$routeParams', 'Configuration',
        ($q, $scope, $location, Logger, PageLifecycle, AppEntityLoader, AuditSvcs, $routeParams, Configuration) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

            var getNextPage = function(successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app, page: $scope.currentPage, count: 20 }, function(results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };

            var pageData = AppEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('AppActivity', 'appView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('app-activity', [ $scope.app.name ]);
            });
        }])

}

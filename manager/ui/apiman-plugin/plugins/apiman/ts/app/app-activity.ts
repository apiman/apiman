/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppActivityController = _module.controller("Apiman.AppActivityController",
        ['$q', '$scope', '$location', 'Logger', 'PageLifecycle', 'AppEntityLoader', 'AuditSvcs', '$routeParams',
        ($q, $scope, $location, Logger, PageLifecycle, AppEntityLoader, AuditSvcs, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;

            var getNextPage = function(successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, entityType: 'applications', entityId: params.app, page: $scope.currentPage, count: 20 }, function(results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };

            var dataLoad = AppEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            var promise = $q.all(dataLoad);
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('AppActivity', promise, $scope, function() {
                PageLifecycle.setPageTitle('app-activity', [ $scope.app.name ]);
            });
        }])

}

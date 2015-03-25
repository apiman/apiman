/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var PlanActivityController = _module.controller("Apiman.PlanActivityController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'AuditSvcs', 'Logger', 'PageLifecycle', 'PlanEntityLoader', 
        ($q, $scope, $location, OrgSvcs, AuditSvcs, Logger, PageLifecycle, PlanEntityLoader) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            $scope.tab = 'activity';
            $scope.version = params.version;

            var getNextPage = function(successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                AuditSvcs.get({ organizationId: params.org, entityType: 'plans', entityId: params.plan, page: $scope.currentPage, count: 20 }, function(results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };

            var dataLoad = PlanEntityLoader.getCommonData($scope, $location);
            dataLoad = angular.extend(dataLoad, {
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            var promise = $q.all(dataLoad);
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('PlanActivity', promise, $scope);
        }])

}

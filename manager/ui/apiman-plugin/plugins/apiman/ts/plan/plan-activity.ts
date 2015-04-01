/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var PlanActivityController = _module.controller("Apiman.PlanActivityController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'AuditSvcs', 'Logger', 'PageLifecycle', 'PlanEntityLoader', '$routeParams',
        ($q, $scope, $location, OrgSvcs, AuditSvcs, Logger, PageLifecycle, PlanEntityLoader, $routeParams) => {
            var params = $routeParams;
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
            PageLifecycle.loadPage('PlanActivity', promise, $scope, function() {
                PageLifecycle.setPageTitle('plan-activity', [ $scope.plan.name ]);
            });
        }])

}

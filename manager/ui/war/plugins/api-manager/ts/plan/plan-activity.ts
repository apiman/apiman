import {_module} from "../apimanPlugin";
import angular = require("angular");

_module.controller("Apiman.PlanActivityController",
    ['$q', '$scope', '$location', 'OrgSvcs', 'AuditSvcs', 'Logger', 'PageLifecycle', 'PlanEntityLoader', '$routeParams',
    function ($q, $scope, $location, OrgSvcs, AuditSvcs, Logger, PageLifecycle, PlanEntityLoader, $routeParams) {
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

        var pageData = PlanEntityLoader.getCommonData($scope, $location);
        pageData = angular.extend(pageData, {
            auditEntries: $q(function(resolve, reject) {
                $scope.currentPage = 0;
                getNextPage(resolve, reject);
            })
        });

        $scope.getNextPage = getNextPage;
        PageLifecycle.loadPage('PlanActivity', 'planView', pageData, $scope, function() {
            PageLifecycle.setPageTitle('plan-activity', [ $scope.plan.name ]);
        });
    }]);

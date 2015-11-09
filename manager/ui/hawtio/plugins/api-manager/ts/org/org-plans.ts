/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var OrgPlansController = _module.controller("Apiman.OrgPlansController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams', 'CurrentUser',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams, CurrentUser) => {
            $scope.tab = 'plans';
            var params = $routeParams;
            $scope.organizationId = params.org;

            if (!CurrentUser.hasPermission(params.org, 'planView')) {
              delete $rootScope['currentUser'];
            }

            $scope.filterPlans = function(value) {
                if (!value) {
                    $scope.filteredPlans = $scope.plans;
                } else {
                    var filtered = [];
                    angular.forEach($scope.plans, function(plan) {
                        if (plan.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(plan);
                        }
                    });
                    $scope.filteredPlans = filtered;
                }
            };
            
            var pageData = {
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function(org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                        resolve(members);
                    }, reject);
                }),
                plans: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans' }, function(plans) {
                        $scope.filteredPlans = plans;
                        resolve(plans);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgPlans', pageData, $scope, function() {
                PageLifecycle.setPageTitle('org-plans', [ $scope.org.name ]);
            });
        }]);

}

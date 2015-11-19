/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var PlanPoliciesController = _module.controller("Apiman.PlanPoliciesController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'ApimanSvcs', 'Logger', 'PageLifecycle', 'PlanEntityLoader', 'Dialogs', '$routeParams', 
        ($q, $scope, $location, OrgSvcs, ApimanSvcs, Logger, PageLifecycle, PlanEntityLoader, Dialogs, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'policies';
            $scope.version = params.version;

            var removePolicy = function(policy) {
                angular.forEach($scope.policies, function(p, index) {
                    if (policy === p) {
                        $scope.policies.splice(index, 1);
                    }
                });
            };

            $scope.removePolicy = function(policy) {
                Logger.info('Removing policy: {0}', policy);
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the plan?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                        removePolicy(policy);
                    }, PageLifecycle.handleError);
                });
            };

            $scope.reorderPolicies = function(reorderedPolicies) {
                var policyChainBean = {
                    policies: reorderedPolicies
                };

                OrgSvcs.save({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'reorderPolicies' },
                    policyChainBean,
                    function() {
                        Logger.debug("Reordering POSTed successfully");
                    }, function() {
                        Logger.debug("Reordering POST failed.")
                    });
            }

            var pageData = PlanEntityLoader.getCommonData($scope, $location);
            angular.extend(pageData, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function(policies) {
                        resolve(policies);
                    }, reject);
                })
            });

            PageLifecycle.loadPage('PlanPolicies', 'planView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('plan-policies', [ $scope.plan.name ]);
            });
        }])

}

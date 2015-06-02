/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AppPoliciesController = _module.controller("Apiman.AppPoliciesController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'AppEntityLoader', 'OrgSvcs', 'Dialogs', '$routeParams',
        ($q, $scope, $location, PageLifecycle, AppEntityLoader, OrgSvcs, Dialogs, $routeParams) => {
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
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the application?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                        removePolicy(policy);
                    }, PageLifecycle.handleError);
                });
            };

            $scope.reorderPolicies = function(reorderedPolicies) {
                var policyChainBean = {
                    policies: reorderedPolicies
                };

                OrgSvcs.save({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'reorderPolicies' },
                    policyChainBean,
                    function() {
                        Logger.debug("Reordering POSTed successfully");
                    }, function() {
                        Logger.debug("Reordering POST failed.")
                    });
            }

            var pageData = AppEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'applications', entityId: params.app, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function(policies) {
                        resolve(policies);
                    }, reject);
                })
            });

            PageLifecycle.loadPage('AppPolicies', pageData, $scope, function() {
                PageLifecycle.setPageTitle('app-policies', [ $scope.app.name ]);
            });
        }])

}

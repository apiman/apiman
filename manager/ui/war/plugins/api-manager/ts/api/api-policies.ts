/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

 export var ApiPoliciesController = _module.controller('Apiman.ApiPoliciesController',
        ['$q', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'Dialogs', '$routeParams', 'Configuration', 'EntityStatusSvc', 'CurrentUser',
        ($q, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, Dialogs, $routeParams, Configuration, EntityStatusSvc, CurrentUser) => {
            var params = $routeParams;

            $scope.organizationId = params.org;
            $scope.tab = 'policies';
            $scope.version = params.version;
            $scope.showMetrics = Configuration.ui.metrics;

            var removePolicy = function(policy) {
                angular.forEach($scope.policies, function(p, index) {
                    if (policy === p) {
                        $scope.policies.splice(index, 1);
                    }
                });
            };

            $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;

            $scope.removePolicy = function(policy) {
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the API?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'apis', entityId:params.api, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                        removePolicy(policy);
                        EntityStatusSvc.getEntity().modifiedOn = Date.now();
                        EntityStatusSvc.getEntity().modifiedBy = CurrentUser.getCurrentUser();
                    }, PageLifecycle.handleError);
                });
            };

            $scope.reorderPolicies = function(reorderedPolicies) {
                var policyChainBean = {
                    policies: reorderedPolicies
                };

                OrgSvcs.save({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'reorderPolicies' },
                    policyChainBean,
                    function() {
                        Logger.debug("Reordering POSTed successfully");
                        EntityStatusSvc.getEntity().modifiedOn = Date.now();
                        EntityStatusSvc.getEntity().modifiedBy = CurrentUser.getCurrentUser();
                    }, function() {
                        Logger.debug("Reordering POST failed.")
                    });
            };

            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            pageData = angular.extend(pageData, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, resolve, reject);
                })
            });


            PageLifecycle.loadPage('ApiPolicies', 'apiView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('api-policies', [ $scope.api.name ]);
            });
        }]);
}

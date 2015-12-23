/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ClientPoliciesController = _module.controller("Apiman.ClientPoliciesController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ClientEntityLoader', 'OrgSvcs', 'Dialogs', '$routeParams', 'Configuration', 'EntityStatusSvc', 'CurrentUser',
        ($q, $scope, $location, PageLifecycle, ClientEntityLoader, OrgSvcs, Dialogs, $routeParams, Configuration, EntityStatusSvc, CurrentUser) => {
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

            $scope.removePolicy = function(policy) {
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the client app?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
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

                OrgSvcs.save({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'reorderPolicies' },
                    policyChainBean,
                    function() {
                        Logger.debug("Reordering POSTed successfully");
                        EntityStatusSvc.getEntity().modifiedOn = Date.now();
                        EntityStatusSvc.getEntity().modifiedBy = CurrentUser.getCurrentUser();
                    }, function() {
                        Logger.debug("Reordering POST failed.")
                    });
            }

            var pageData = ClientEntityLoader.getCommonData($scope, $location);
            pageData = angular.extend(pageData, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, function(policies) {
                        resolve(policies);
                    }, reject);
                })
            });

            PageLifecycle.loadPage('ClientPolicies', 'clientView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('client-policies', [ $scope.client.name ]);
            });
        }])

}

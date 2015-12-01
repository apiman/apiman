/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServicePoliciesController = _module.controller('Apiman.ServicePoliciesController',
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'Dialogs', '$routeParams', 'Configuration', 'EntityStatusService',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, Dialogs, $routeParams, Configuration, EntityStatusService) => {
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

            $scope.isEntityDisabled = EntityStatusService.isEntityDisabled;

            $scope.removePolicy = function(policy) {
                Dialogs.confirm('Confirm Remove Policy', 'Do you really want to remove this policy from the service?', function() {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                        removePolicy(policy);
                    }, PageLifecycle.handleError);
                });
            };

            $scope.reorderPolicies = function(reorderedPolicies) {
                var policyChainBean = {
                    policies: reorderedPolicies
                };

                OrgSvcs.save({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'reorderPolicies' },
                    policyChainBean,
                    function() {
                        Logger.debug("Reordering POSTed successfully");
                    }, function() {
                        Logger.debug("Reordering POST failed.")
                    });
            };

            var pageData = ServiceEntityLoader.getCommonData($scope, $location);

            pageData = angular.extend(pageData, {
                policies: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies' }, resolve, reject);
                })
            });


            PageLifecycle.loadPage('ServicePolicies', 'svcView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('service-policies', [ $scope.service.name ]);
            });
        }]);
}

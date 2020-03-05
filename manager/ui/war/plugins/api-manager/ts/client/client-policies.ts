/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ClientPoliciesController = _module.controller("Apiman.ClientPoliciesController",
        [
            '$q',
            '$scope',
            '$location',
            '$uibModal',
            'PageLifecycle',
            'ClientEntityLoader',
            'OrgSvcs',
            '$routeParams',
            'Configuration',
            'EntityStatusSvc',
            'CurrentUser',
        ($q, $scope, $location, $uibModal, PageLifecycle, ClientEntityLoader, OrgSvcs, $routeParams, Configuration, EntityStatusSvc, CurrentUser) => {
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

            $scope.removePolicy = function(policy, size) {
                var options = {
                    title: 'Confirm Remove Policy',
                    message: 'Do you really want to remove this policy from the client app?'
                };
    
                $scope.animationsEnabled = true;
    
                $scope.toggleAnimation = function () {
                    $scope.animationsEnabled = !$scope.animationsEnabled;
                };
    
                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'confirmModal.html',
                    controller: 'ModalConfirmCtrl',
                    size: size,
                    resolve: {
                        options: function () {
                            return options;
                        }
                    }
                });
    
                modalInstance.result.then(function () {
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
                        removePolicy(policy);
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
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
                    }, function() {
                        Logger.debug("Reordering POST failed.")
                    });
            };

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

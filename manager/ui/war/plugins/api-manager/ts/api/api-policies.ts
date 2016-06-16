/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

 export var ApiPoliciesController = _module.controller('Apiman.ApiPoliciesController',
        [
            '$q',
            '$scope',
            '$location',
            '$uibModal',
            'PageLifecycle',
            'ActionSvcs',
            'ApiEntityLoader',
            'OrgSvcs',
            '$routeParams',
            'Configuration',
            'EntityStatusSvc',
            'CurrentUser',
        ($q, $scope, $location, $uibModal, PageLifecycle, ActionSvcs, ApiEntityLoader, OrgSvcs, $routeParams, Configuration, EntityStatusSvc, CurrentUser) => {
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

            $scope.removePolicy = function(policy, size) {

                var options = {
                    message: 'Do you really want to remove this policy from the API?',
                    title: 'Confirm Remove Policy'
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
                    OrgSvcs.delete({
                        organizationId: params.org,
                        entityType: 'apis',
                        entityId:params.api,
                        versionsOrActivity: 'versions',
                        version: params.version,
                        policiesOrActivity: 'policies',
                        policyId: policy.id
                    }, function(reply) {
                        removePolicy(policy);
                        EntityStatusSvc.getEntity().modifiedOn = Date.now();
                        EntityStatusSvc.getEntity().modifiedBy = CurrentUser.getCurrentUser();
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
                    $scope.unregisterButton.state = 'complete';
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

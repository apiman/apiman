/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var PlanPoliciesController = _module.controller("Apiman.PlanPoliciesController",
        ['$q', '$scope', '$location', '$uibModal', 'OrgSvcs', 'ApimanSvcs', 'Logger', 'PageLifecycle', 'PlanEntityLoader', '$routeParams',
        ($q, $scope, $location, $uibModal, OrgSvcs, ApimanSvcs, Logger, PageLifecycle, PlanEntityLoader, $routeParams) => {
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

            $scope.removePolicy = function(policy, size) {
                Logger.info('Removing policy: {0}', policy);

                var options = {
                    message: 'Do you really want to remove this policy from the plan?',
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
                    OrgSvcs.delete({ organizationId: params.org, entityType: 'plans', entityId: params.plan, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'policies', policyId: policy.id }, function(reply) {
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

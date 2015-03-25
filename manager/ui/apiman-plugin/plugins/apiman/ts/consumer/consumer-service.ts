/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ConsumerSvcController = _module.controller("Apiman.ConsumerSvcController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', ($q, $scope, $location, OrgSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.params = params;
            $scope.chains = {};
            
            $scope.getPolicyChain = function(plan) {
                var planId = plan.planId;
                if (!$scope.chains[planId]) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'plans', policyId: plan.planId, policyChain : 'policyChain' }, function(policyReply) {
                        $scope.chains[planId] = policyReply.policies;
                    }, function(error) {
                        $scope.chains[planId] = [];
                    });
                }
            };
            
            var dataPackets:any = {
                service: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service }, resolve, reject);
                })
            };
            var promise = undefined;
            if (params.version) {
                dataPackets.version = $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, resolve, reject);
                });
                dataPackets.versions = $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions' }, function(versions) {
                        angular.forEach(versions, function(version) {
                            if (params.version == version.version) {
                                $scope.selectedServiceVersion = version;
                            }
                        });
                        resolve(versions);
                    }, reject);
                });
                dataPackets.publicEndpoint = $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'endpoint' }, resolve, function(error) {
                        resolve({
                            managedEndpoint: 'Not available.'
                        });
                    });
                });
                dataPackets.plans = $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version, policiesOrActivity: 'plans' }, resolve, reject);
                });
            } else {
                dataPackets.versions = $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions' }, function(versions) {
                        if (versions.length > 0) {
                            $location.search('version', versions[0].version).replace();
                        }
                        resolve(versions);
                    }, reject);
                });
            }
            
            $scope.setVersion = function(serviceVersion) {
                $location.search('version', serviceVersion.version);
            };

            var promise = $q.all(dataPackets);
            
            PageLifecycle.loadPage('ConsumerService', promise, $scope);
        }])

}

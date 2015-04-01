/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ConsumerServiceRedirectController = _module.controller("Apiman.ConsumerServiceRedirectController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $routeParams) => {
            var orgId = $routeParams.org;
            var serviceId = $routeParams.service;
            var promise = $q.all({
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'services', entityId: serviceId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            });
            PageLifecycle.loadPage('ConsumerServiceRedirect', promise, $scope, function() {
                var version = $scope.versions[0].version;
                PageLifecycle.forwardTo('/browse/orgs/{0}/{1}/{2}', orgId, serviceId, version);
            });
        }]);

    
    export var ConsumerSvcController = _module.controller("Apiman.ConsumerSvcController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $routeParams) => {
            $scope.params = $routeParams;
            $scope.chains = {};
            
            $scope.getPolicyChain = function(plan) {
                var planId = plan.planId;
                if (!$scope.chains[planId]) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'plans', policyId: plan.planId, policyChain : 'policyChain' }, function(policyReply) {
                        $scope.chains[planId] = policyReply.policies;
                    }, function(error) {
                        $scope.chains[planId] = [];
                    });
                }
            };
            
            var promise = $q.all({
                service: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service }, resolve, reject);
                }),
                version: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version }, resolve, reject);
                }),
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions' }, function(versions) {
                        angular.forEach(versions, function(version) {
                            if (version.version == $routeParams.version) {
                                $scope.selectedServiceVersion = version;
                            }
                        });
                        resolve(versions);
                    }, reject);
                }),
                publicEndpoint: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'endpoint' }, resolve, function(error) {
                        resolve({
                            managedEndpoint: 'Not available.'
                        });
                    });
                }),
                plans: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'plans' }, resolve, reject);
                })
            });
            
            $scope.setVersion = function(serviceVersion) {
                PageLifecycle.redirectTo('/browse/orgs/{0}/{1}/{2}', $routeParams.org, $routeParams.service, $routeParams.version);
            };

            PageLifecycle.loadPage('ConsumerService', promise, $scope, function() {
                PageLifecycle.setPageTitle('consumer-service', [ $scope.service.name ]);
            });
        }])

}

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {
    
    export var ConsumerServiceRedirectController = _module.controller("Apiman.ConsumerServiceRedirectController",
        ['$q', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, OrgSvcs, PageLifecycle, $routeParams) => {
            var orgId = $routeParams.org;
            var serviceId = $routeParams.service;
            var pageData = {
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'services', entityId: serviceId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };
            PageLifecycle.loadPage('ConsumerServiceRedirect', pageData, $scope, function() {
                var version = $scope.versions[0].version;
                PageLifecycle.forwardTo('/browse/orgs/{0}/{1}/{2}', orgId, serviceId, version);
            });
        }]);

    
    export var ConsumerSvcController = _module.controller("Apiman.ConsumerSvcController",
        ['$q', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, OrgSvcs, PageLifecycle, $routeParams) => {
            $scope.params = $routeParams;
            $scope.chains = {};
            
            $scope.hasSwagger = false;
            try {
                var swagger = SwaggerUi;
                $scope.hasSwagger = true;
            } catch (e) {}
            
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
            
            var pageData = {
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
            };
            
            $scope.setVersion = function(serviceVersion) {
                PageLifecycle.redirectTo('/browse/orgs/{0}/{1}/{2}', $routeParams.org, $routeParams.service, serviceVersion.version);
            };

            PageLifecycle.loadPage('ConsumerService', pageData, $scope, function() {
                $scope.service = $scope.version.service;
                $scope.org = $scope.service.organization;
                PageLifecycle.setPageTitle('consumer-service', [ $scope.service.name ]);
            });
        }]);

    export var ConsumerSvcDefController = _module.controller("Apiman.ConsumerSvcDefController",
        ['$q', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams', '$window', 'Logger', 'ServiceDefinitionSvcs', 'Configuration',
        ($q, $scope, OrgSvcs, PageLifecycle, $routeParams, $window, Logger, ServiceDefinitionSvcs, Configuration) => {
            $scope.params = $routeParams;
            $scope.chains = {};

            var pageData = {
                version: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'services', entityId: $routeParams.service, versionsOrActivity: 'versions', version: $routeParams.version }, resolve, reject);
                })
            };
            
            PageLifecycle.loadPage('ConsumerServiceDef', pageData, $scope, function() {
                $scope.service = $scope.version.service;
                $scope.org = $scope.service.organization;
                $scope.hasError = false;

                PageLifecycle.setPageTitle('consumer-service-def', [ $scope.service.name ]);
                
                var hasSwagger = false;
                try {
                    var swagger = SwaggerUi;
                    hasSwagger = true;
                } catch (e) {}

                if ($scope.version.definitionType == 'SwaggerJSON' && hasSwagger) {
                    var url = ServiceDefinitionSvcs.getServiceDefinitionUrl($scope.params.org, $scope.params.service, $scope.params.version);
                    Logger.debug("!!!!! Using definition URL: {0}", url);

                    var authHeader = Configuration.getAuthorizationHeader();
                    
                    $scope.definitionStatus = 'loading';
                    var swaggerOptions = {
                        url: url,
                        dom_id:"swagger-ui-container",
                        validatorUrl:null,
                        sorter : "alpha",
                        authorizations: {
                            apimanauth: new SwaggerClient.ApiKeyAuthorization("Authorization", authHeader, "header")
                        },
                        onComplete: function() {
                            $('#swagger-ui-container a').each(function(idx, elem) {
                                var href = $(elem).attr('href');
                                if (href[0] == '#') {
                                    $(elem).removeAttr('href');
                                }
                            });
                            $('#swagger-ui-container div.sandbox_header').each(function(idx, elem) {
                                $(elem).remove();
                            });
                            $('#swagger-ui-container li.operation div.auth').each(function(idx, elem) {
                                $(elem).remove();
                            });
                            $('#swagger-ui-container li.operation div.access').each(function(idx, elem) {
                                $(elem).remove();
                            });
                            $scope.$apply(function(error) {
                                $scope.definitionStatus = 'complete';
                            });
                        },
                        onFailure: function() {
                            $scope.$apply(function(error) {
                                $scope.definitionStatus = 'error';
                                $scope.hasError = true;
                                $scope.error = error;
                            });
                        }
                    };
                    $window.swaggerUi = new SwaggerUi(swaggerOptions);
                    $window.swaggerUi.load();
                    $scope.hasDefinition = true;
                } else {
                    $scope.hasDefinition = false;
                }
            });
        }]);

}

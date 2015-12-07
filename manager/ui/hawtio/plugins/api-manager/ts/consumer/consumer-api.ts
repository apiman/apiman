/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {
    
    export var ConsumerApiRedirectController = _module.controller("Apiman.ConsumerApiRedirectController",
        ['$q', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, OrgSvcs, PageLifecycle, $routeParams) => {
            var orgId = $routeParams.org;
            var apiId = $routeParams.api;
            var pageData = {
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'apis', entityId: apiId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };
            PageLifecycle.loadPage('ConsumerApiRedirect', undefined, pageData, $scope, function() {
                var version = $scope.versions[0].version;
                for (var i = 0; i < $scope.versions.length; i++) {
                	var v = $scope.versions[i];
                	if (v.status == 'Published') {
                		version = v.version;
                		break;
                	}
                }
                PageLifecycle.forwardTo('/browse/orgs/{0}/{1}/{2}', orgId, apiId, version);
            });
        }]);

    
    export var ConsumerApiController = _module.controller("Apiman.ConsumerApiController",
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
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'apis', entityId: $routeParams.api, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'plans', policyId: plan.planId, policyChain : 'policyChain' }, function(policyReply) {
                        $scope.chains[planId] = policyReply.policies;
                    }, function(error) {
                        $scope.chains[planId] = [];
                    });
                }
            };
            
            var pageData = {
                version: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'apis', entityId: $routeParams.api, versionsOrActivity: 'versions', version: $routeParams.version }, resolve, reject);
                }),
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'apis', entityId: $routeParams.api, versionsOrActivity: 'versions' }, function(versions) {
                    	var publishedVersions = [];
                        angular.forEach(versions, function(version) {
                            if (version.version == $routeParams.version) {
                                $scope.selectedApiVersion = version;
                            }
                            if (version.status == 'Published') {
                            	publishedVersions.push(version);
                            }
                        });
                        resolve(publishedVersions);
                    }, reject);
                }),
                publicEndpoint: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'apis', entityId: $routeParams.api, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'endpoint' }, resolve, function(error) {
                        resolve({
                            managedEndpoint: 'Not available.'
                        });
                    });
                }),
                plans: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'apis', entityId: $routeParams.api, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'plans' }, resolve, reject);
                })
            };
            
            $scope.setVersion = function(apiVersion) {
                PageLifecycle.redirectTo('/browse/orgs/{0}/{1}/{2}', $routeParams.org, $routeParams.api, apiVersion.version);
            };

            PageLifecycle.loadPage('ConsumerApi', undefined, pageData, $scope, function() {
                $scope.api = $scope.version.api;
                $scope.org = $scope.api.organization;
                PageLifecycle.setPageTitle('consumer-api', [ $scope.api.name ]);
            });


            // Tooltip

            $scope.tooltipTxt = 'Copy to clipboard';

            // Called on clicking the button the tooltip is attached to
            $scope.tooltipChange = function() {
                $scope.tooltipTxt = 'Copied!';
            };

            // Call when the mouse leaves the button the tooltip is attached to
            $scope.tooltipReset = function() {
                setTimeout(function() {
                    $scope.tooltipTxt = 'Copy to clipboard';
                }, 100);
            };


            // Copy-to-Clipboard

            // Called if copy-to-clipboard functionality was successful
            $scope.copySuccess = function () {
                //console.log('Copied!');
            };

            // Called if copy-to-clipboard functionality was unsuccessful
            $scope.copyFail = function (err) {
                //console.error('Error!', err);
            };
        }]);

    export var ConsumerApiDefController = _module.controller("Apiman.ConsumerApiDefController",
        ['$q', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams', '$window', 'Logger', 'ApiDefinitionSvcs', 'Configuration',
        ($q, $scope, OrgSvcs, PageLifecycle, $routeParams, $window, Logger, ApiDefinitionSvcs, Configuration) => {
            $scope.params = $routeParams;
            $scope.chains = {};

            var pageData = {
                version: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'apis', entityId: $routeParams.api, versionsOrActivity: 'versions', version: $routeParams.version }, resolve, reject);
                })
            };
            
            PageLifecycle.loadPage('ConsumerApiDef', undefined, pageData, $scope, function() {
                $scope.api = $scope.version.api;
                $scope.org = $scope.api.organization;
                $scope.hasError = false;

                PageLifecycle.setPageTitle('consumer-api-def', [ $scope.api.name ]);
                
                var hasSwagger = false;
                try {
                    var swagger = SwaggerUi;
                    hasSwagger = true;
                } catch (e) {}

                if ($scope.version.definitionType == 'SwaggerJSON' && hasSwagger) {
                    var url = ApiDefinitionSvcs.getApiDefinitionUrl($scope.params.org, $scope.params.api, $scope.params.version);
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

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
        ['$q', '$rootScope', '$scope', 'OrgSvcs', 'PageLifecycle', '$routeParams', '$window', 'Logger', 'ApiDefinitionSvcs', 'Configuration', 'SwaggerUIContractService',
        ($q, $rootScope, $scope, OrgSvcs, PageLifecycle, $routeParams, $window, Logger, ApiDefinitionSvcs, Configuration, SwaggerUIContractService) => {
            $scope.params = $routeParams;
            $scope.chains = {};

            var pageData = {
                version: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'apis', entityId: $routeParams.api, versionsOrActivity: 'versions', version: $routeParams.version }, resolve, reject);
                }),
                publicEndpoint: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: 'apis', entityId: $routeParams.api, versionsOrActivity: 'versions', version: $routeParams.version, policiesOrActivity: 'endpoint' }, resolve, function(error) {
                        resolve({
                            managedEndpoint: 'Not available.'
                        });
                    });
                })
            };

            const DisableTryItOutPlugin = function() {
                return {
                    statePlugins: {
                        spec: {
                            wrapSelectors: {
                                allowTryItOutFor: () => () => false
                            }
                        }
                    }
                }
            };

            // SwaggerUI Plugins
            const DisableAuthorizePlugin = function() {
                return {
                    wrapComponents: {
                        authorizeBtn: () => () => null
                    }
                }
            };

            $scope.$on("$locationChangeStart", function(event, new_url, old_url){
                //check if new requested url contains /def
                //If not remove X-API-Key
                if (new_url.indexOf("/def") == -1){
                    // console.log("Success");
                    SwaggerUIContractService.removeXAPIKey();
                }
            });

            PageLifecycle.loadPage('ConsumerApiDef', undefined, pageData, $scope, function() {
                $scope.api = $scope.version.api;
                $scope.org = $scope.api.organization;
                $scope.hasError = false;

                let hasPublicPublishedAPI = ($scope.version.publicAPI && $scope.version.status == "Published") ? true : false;
                let hasContract = (SwaggerUIContractService.getXAPIKey() && $scope.version.status == "Published") ? true : false;

                PageLifecycle.setPageTitle('consumer-api-def', [ $scope.api.name ]);

                if (($scope.version.definitionType == 'SwaggerJSON' || $scope.version.definitionType == 'SwaggerYAML') && SwaggerUIBundle) {
                    var url = ApiDefinitionSvcs.getApimanDefinitionUrl($scope.params.org, $scope.params.api, $scope.params.version);
                    Logger.debug("!!!!! Using definition URL: {0}", url);

                    $scope.definitionStatus = 'loading';
                    let ui;
                    let swaggerOptions = <any>{
                        url: url,
                        dom_id: "#swagger-ui-container",
                        validatorUrl: "https://online.swagger.io/validator",
                        presets: [
                            SwaggerUIBundle.presets.apis
                        ],
                        layout: "BaseLayout",
                        sorter : "alpha",

                        requestInterceptor: function(request) {
                            // Only add auth header to requests where the URL matches the one specified above.
                            if (request.url === url) {
                                request.headers.Authorization = Configuration.getAuthorizationHeader();
                            }

                            // Send the API-Key always as parameter if the URL is not the URL whrere the SwaggerFile comes from
                            // to avoid CORS Problems with the backend
                            if (SwaggerUIContractService.getXAPIKey() && request.url !== url){
                                let requestUrl = URI(request.url);
                                requestUrl.addSearch("apikey", SwaggerUIContractService.getXAPIKey());
                                request.url = requestUrl.toString();
                            }
                            return request;
                        },
                        onComplete: function() {
                            $scope.$apply(function() {
                                $scope.definitionStatus = 'complete';
                            });

                            if (SwaggerUIContractService.getXAPIKey()){
                                ui.preauthorizeApiKey("X-API-Key", SwaggerUIContractService.getXAPIKey());
                            }
                        },
                        // do error handling in the responseInterceptor
                        responseInterceptor: function (response) {
                            if (response.status == 500 && response.ok === false) {
                                $scope.$apply(function() {
                                    $scope.definitionStatus = 'error';
                                    $scope.hasError = true;
                                });
                            }
                            return response;
                        }
                    };

                    // Remove try-out and authorize if the API is not public or has no contract
                    if (!(hasContract || hasPublicPublishedAPI)){
                        swaggerOptions.plugins = [];
                        swaggerOptions.plugins.push(DisableTryItOutPlugin, DisableAuthorizePlugin);
                    }

                    ui = SwaggerUIBundle(swaggerOptions);
                    $scope.hasDefinition = true;
                } else {
                    $scope.hasDefinition = false;
                }
            });
        }]);

}

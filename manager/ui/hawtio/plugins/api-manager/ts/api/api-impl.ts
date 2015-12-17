/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

 export var ApiImplController = _module.controller("Apiman.ApiImplController",
        ['$q', '$rootScope', '$scope', '$location', 'PageLifecycle', 'ApiEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'EntityStatusSvc', 'Logger', 'Configuration',
        ($q, $rootScope, $scope, $location, PageLifecycle, ApiEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, EntityStatusSvc, Logger, Configuration) => {
            var params = $routeParams;

            $scope.organizationId = params.org;
            $scope.tab = 'impl';
            $scope.version = params.version;
            $scope.typeOptions = ["rest", "soap"];
            $scope.contentTypeOptions = ["json", "xml"];
            $scope.updatedApi = new Object();
            $scope.apiSecurity = new Object();
            $scope.showMetrics = Configuration.ui.metrics;
            $scope.selectedGateway = {val: null};

            $scope.saved = false;
            $scope.saving = false;

            var pageData = ApiEntityLoader.getCommonData($scope, $location);

            if (params.version != null) {
                pageData = angular.extend(pageData, {
                    gateways: $q(function(resolve, reject) {
                        ApimanSvcs.query({ entityType: 'gateways' }, resolve, reject);
                    })
                });
            }

            $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;

            // API Security Type Options
            $scope.apiSecurityTypeOptions = [
                {
                    label: 'None',
                    i18nKey: 'none',
                    type: 'none'
                },
                {
                    label: 'MTLS/Two-Way-SSL',
                    i18nKey: 'mtls',
                    type: 'mtls'
                },
                {
                    label: 'BASIC Authentication',
                    i18nKey: 'basic-auth',
                    type: 'basic'
                }
            ];
            
            var epValue = function(endpointProperties, key) {
                if (endpointProperties && endpointProperties[key]) {
                    return endpointProperties[key];
                } else {
                    return null;
                }
            };
            
            var toApiSecurity = function(version) {
                var rval:any = {};
                rval.type = version.endpointProperties['authorization.type'];
                if (!rval.type) {
                    rval.type = 'none';
                }
                if (rval.type == 'mssl') {
                    rval.type = 'mtls';
                }
                if (rval.type == 'basic') {
                    rval.basic = {
                        username: epValue(version.endpointProperties, 'basic-auth.username'),
                        password: epValue(version.endpointProperties, 'basic-auth.password'),
                        confirmPassword: epValue(version.endpointProperties, 'basic-auth.password'),
                        requireSSL: 'true' === epValue(version.endpointProperties, 'basic-auth.requireSSL')
                    };
                }
                return rval;
            };
            
            var toEndpointProperties = function(apiSecurity) {
                var rval:any = {};
                if (apiSecurity.type == 'none') {
                    return rval;
                }
                rval['authorization.type'] = apiSecurity.type;
                if (apiSecurity.type == 'basic' && apiSecurity.basic) {
                    rval['basic-auth.username'] = apiSecurity.basic.username;
                    rval['basic-auth.password'] = apiSecurity.basic.password;
                    if (apiSecurity.basic.requireSSL) {
                        rval['basic-auth.requireSSL'] = 'true';
                    } else {
                        rval['basic-auth.requireSSL'] = 'false';
                    }
                }
                return rval;
            };

            // Validates depending on the endpoint type selected
            $scope.checkValid = function() {
                var valid = true;
                if (!$scope.updatedApi.endpointType) {
                    valid = false;
                }

                if ($scope.apiSecurity.type == 'basic' && $scope.apiSecurity.basic) {
                    if (!$scope.apiSecurity.basic.password) {
                        valid = false;
                    }

                    if ($scope.apiSecurity.basic.password != $scope.apiSecurity.basic.confirmPassword) {
                        valid = false;
                    }
                } else if ($scope.apiSecurity.type == 'basic' && !$scope.apiSecurity.basic) {
                    valid = false;
                }

                $scope.isValid = valid;

                return valid;
            };

            // This function checks for changes to the updateApi model
            // and compares the new value to the original value. If they are all the same,
            // the Save button will remain disabled.
            $scope.$watch('updatedApi', function(newValue) {
                if ($scope.version) {
                    var dirty = false;
                    
                    if (newValue.endpoint != $scope.version.endpoint) {
                        dirty = true;
                    }
                    if (newValue.endpointType != $scope.version.endpointType) {
                        dirty = true;
                    }
                    if (newValue.endpointContentType != $scope.version.endpointContentType) {
                        dirty = true;
                    }
                    
                    if (newValue.gateways && newValue.gateways.length > 0) {
                        dirty = true;
                    }
                    if ($scope.version.endpointProperties && newValue.endpointProperties) {
                        if (!angular.equals($scope.version.endpointProperties, newValue.endpointProperties)) {
                            Logger.debug('Dirty due to EP:');
                            Logger.debug('    $scope.version:    {0}', $scope.version);
                            Logger.debug('    $scope.version.EP: {0}', $scope.version.endpointProperties);
                            Logger.debug('    newValue.EP:       {0}', newValue.endpointProperties);
                            dirty = true;
                        }
                    }

                    $scope.checkValid();
                    
                    $rootScope.isDirty = dirty;
                }
            }, true);


            $scope.$watch('selectedGateway.val', function(newValue) {
                if (newValue) {
                    var alreadySet = false;
                    if ($scope.version.gateways && $scope.version.gateways.length > 0 && $scope.version.gateways[0].gatewayId == newValue.id) {
                        alreadySet = true;
                    }
                    if (!alreadySet) {
                        $scope.updatedApi.gateways = [ { gatewayId: newValue.id } ];
                    } else {
                        delete $scope.updatedApi.gateways;
                    }
                }
            });

            $scope.setEndpointProperties = function(newValue) {
                if (newValue) {
                    $scope.updatedService.endpointProperties = toEndpointProperties(newValue);
                    $scope.checkValid();
                }
            };

            $scope.setGateways = function(newValue) {
                if($scope.version.gateways && $scope.version.gateways.length > 0) {
                    $rootScope.isDirty = (newValue.id != $scope.version.gateways[0].gatewayId);
                }
            };

            $scope.reset = function() {
                if (!$scope.version.endpointType) {
                  $scope.version.endpointType = 'rest';
                }

                if (!$scope.version.endpointContentType) {
                    $scope.version.endpointContentType = 'json';
                }

                $scope.apiSecurity = toApiSecurity($scope.version);
                $scope.updatedApi.endpoint = $scope.version.endpoint;
                $scope.updatedApi.endpointType = $scope.version.endpointType;
                $scope.updatedApi.endpointContentType = $scope.version.endpointContentType;
                $scope.updatedApi.endpointProperties = angular.copy($scope.version.endpointProperties);

                delete $scope.updatedApi.gateways;

                if ($scope.version.gateways && $scope.version.gateways.length > 0) {
                    angular.forEach($scope.gateways, function(gateway) {
                        // TODO support multiple gateway assignments here
                        if (gateway.id == $scope.version.gateways[0].gatewayId) {
                            $scope.selectedGateway.val = gateway;
                        }
                    });
                }

                $rootScope.isDirty = false;
            };

            $scope.saveApi = function() {
                $scope.invalidEndpoint = false;
                $scope.saving = true;
                
                OrgSvcs.update({ organizationId: params.org, entityType: 'apis', entityId:params.api, versionsOrActivity: 'versions', version: params.version }, $scope.updatedApi, function(reply) {
                    $rootScope.isDirty = false;
                    $scope.autoGateway = false;
                    $scope.saved = true;
                    $scope.saving = false;
                    $scope.version = reply;

                    EntityStatusSvc.setEntityStatus(reply.status);
                }, PageLifecycle.handleError);
            };


            // Endpoint Validation
            $scope.invalidEndpoint = false;

            $scope.validateEndpoint = function() {
                var first7 = $scope.updatedApi.endpoint.substring(0, 7);
                var first8 = $scope.updatedApi.endpoint.substring(0, 8);

                var re = new RegExp('^(http|https):\/\/', 'i');

                // Test first 7 letters for http:// first
                if(re.test(first7) === true) {
                    $scope.saveApi();
                } else {
                    // If it fails, test first 8 letters for https:// next
                    if(re.test(first8) === true) {
                        $scope.saveApi();
                    } else {
                        console.log('Invalid input.');
                        $scope.invalidEndpoint = true;
                    }
                }
            };

            PageLifecycle.loadPage('ApiImpl', 'apiView', pageData, $scope, function() {
                $scope.reset();
                PageLifecycle.setPageTitle('api-impl', [ $scope.api.name ]);
                
                // Automatically set the selected gateway if there's only one and the 
                // gateway is not already set.
                if (!$scope.version.gateways || $scope.version.gateways.length == 0) {
                    $scope.selectedGateway.val = $scope.gateways[0];
                    $scope.autoGateway = true;
                }
            });
        }]);
}

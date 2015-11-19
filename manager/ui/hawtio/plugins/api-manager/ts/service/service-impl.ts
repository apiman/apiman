/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceImplController = _module.controller("Apiman.ServiceImplController",
        ['$q', '$rootScope', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'EntityStatusService', 'Logger', 'Configuration',
        ($q, $rootScope, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, EntityStatusService, Logger, Configuration) => {
            var params = $routeParams;

            $scope.organizationId = params.org;
            $scope.tab = 'impl';
            $scope.version = params.version;
            $scope.typeOptions = ["rest", "soap"];
            $scope.contentTypeOptions = ["json", "xml"];
            $scope.updatedService = new Object();
            $scope.apiSecurity = new Object();
            $scope.showMetrics = Configuration.ui.metrics;

            var pageData = ServiceEntityLoader.getCommonData($scope, $location);

            if (params.version != null) {
                pageData = angular.extend(pageData, {
                    gateways: $q(function(resolve, reject) {
                        ApimanSvcs.query({ entityType: 'gateways' }, resolve, reject);
                    })
                });
            }

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };
            
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
            
            var checkValid = function() {
                var valid = true;
                if (!$scope.updatedService.endpointType) {
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
            };

            $scope.$watch('updatedService', function(newValue) {
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
                    
                    checkValid();
                    
                    $rootScope.isDirty = dirty;
                }
            }, true);
            
            $scope.$watch('apiSecurity', function(newValue) {
                if (newValue) {
                    $scope.updatedService.endpointProperties = toEndpointProperties(newValue);
                    checkValid();
                }
            }, true);

            $scope.$watch('selectedGateway', function(newValue) {
                if (newValue) {
                    var alreadySet = false;
                    if ($scope.version.gateways && $scope.version.gateways.length > 0 && $scope.version.gateways[0].gatewayId == newValue.id) {
                        alreadySet = true;
                    }
                    if (!alreadySet) {
                        $scope.updatedService.gateways = [ { gatewayId: newValue.id } ];
                    } else {
                        delete $scope.updatedService.gateways;
                    }
                }
            });

            $scope.reset = function() {
                if (!$scope.version.endpointType) {
                  $scope.version.endpointType = 'rest';
                }
                if (!$scope.version.endpointContentType) {
                    $scope.version.endpointContentType = 'json';
                  }
                $scope.apiSecurity = toApiSecurity($scope.version);
                $scope.updatedService.endpoint = $scope.version.endpoint;
                $scope.updatedService.endpointType = $scope.version.endpointType;
                $scope.updatedService.endpointContentType = $scope.version.endpointContentType;
                $scope.updatedService.endpointProperties = angular.copy($scope.version.endpointProperties);
                delete $scope.updatedService.gateways;
                if ($scope.version.gateways && $scope.version.gateways.length > 0) {
                    angular.forEach($scope.gateways, function(gateway) {
                        // TODO support multiple gateway assignments here
                        if (gateway.id == $scope.version.gateways[0].gatewayId) {
                            $scope.selectedGateway = gateway;
                        }
                    });
                }
                $rootScope.isDirty = false;
            };

            $scope.saveService = function() {
                $scope.saveButton.state = 'in-progress';
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version }, $scope.updatedService, function(reply) {
                    $rootScope.isDirty = false;
                    $scope.autoGateway = false;
                    $scope.saveButton.state = 'complete';
                    $scope.version = reply;
                    EntityStatusService.setEntityStatus(reply.status);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('ServiceImpl', 'svcView', pageData, $scope, function() {
                $scope.reset();
                PageLifecycle.setPageTitle('service-impl', [ $scope.service.name ]);
                
                // Automatically set the selected gateway if there's only one and the 
                // gateway is not already set.
                if (!$scope.version.gateways || $scope.version.gateways.length == 0) {
                    if ($scope.gateways && $scope.gateways.length == 1) {
                        $scope.selectedGateway = $scope.gateways[0];
                        $scope.autoGateway = true;
                    }
                }
            });
        }]);
}

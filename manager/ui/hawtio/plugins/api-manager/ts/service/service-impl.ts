/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

 export var ServiceImplController = _module.controller("Apiman.ServiceImplController",
        ['$q', '$scope', '$location', 'PageLifecycle', 'ServiceEntityLoader', 'OrgSvcs', 'ApimanSvcs', '$routeParams', 'EntityStatusService', 'Logger',
        ($q, $scope, $location, PageLifecycle, ServiceEntityLoader, OrgSvcs, ApimanSvcs, $routeParams, EntityStatusService, Logger) => {
            var params = $routeParams;
            $scope.organizationId = params.org;
            $scope.tab = 'impl';
            $scope.version = params.version;
            $scope.typeOptions = ["rest", "soap"];
            $scope.updatedService = new Object();
            $scope.apiSecurity = new Object();

            var pageData = ServiceEntityLoader.getCommonData($scope, $location);
            if (params.version != null) {
                pageData = angular.extend(pageData, {
                    gateways: $q(function(resolve, reject) {
                        ApimanSvcs.query({ entityType: 'gateways' }, resolve, reject);
                    })
                });
            }
            
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
                    rval['basic-auth.requireSSL'] = apiSecurity.basic.requireSSL;
                }
                return rval;
            };

            $scope.$watch('updatedService', function(newValue) {
                if ($scope.version) {
                    var dirty = false;
                    
                    if (newValue.endpoint != $scope.version.endpoint || newValue.endpointType != $scope.version.endpointType) {
                        dirty = true;
                    } else if (newValue.gateways && newValue.gateways.length > 0) {
                        dirty = true;
                    }
                    if ($scope.version.endpointProperties && newValue.endpointProperties) {
                        if (!angular.equals($scope.version.endpointProperties, newValue.endpointProperties)) {
                            dirty = true;
                        }
                    }
                    
                    $scope.isDirty = dirty;
                }
            }, true);
            
            $scope.$watch('apiSecurity', function(newValue) {
                if (newValue) {
                    $scope.updatedService.endpointProperties = toEndpointProperties(newValue);
                    var valid = true;
                    if (newValue.type == 'basic' && newValue.basic) {
                        if (!newValue.basic.password) {
                            valid = false;
                        }
                        if (newValue.basic.password != newValue.basic.confirmPassword) {
                            valid = false;
                        }
                    }
                    $scope.isValid = valid;
                }
            }, true);

            $scope.$watch('selectedGateway', function(newValue) {
                if (newValue) {
                    var alreadySet = false;
                    if ($scope.version.gateways[0].gatewayId == newValue.id) {
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
                $scope.updatedService.endpoint = $scope.version.endpoint;
                $scope.updatedService.endpointType = $scope.version.endpointType;
                $scope.updatedService.endpointProperties = angular.copy($scope.version.endpointProperties);
                delete $scope.updatedService.gateways;
                if ($scope.version.gateways) {
                    angular.forEach($scope.gateways, function(gateway) {
                        // TODO support multiple gateway assignments here
                        if (gateway.id == $scope.version.gateways[0].gatewayId) {
                            $scope.selectedGateway = gateway;
                        }
                    });
                }
                $scope.isDirty = false;
                $scope.apiSecurity = toApiSecurity($scope.version);
            };

            $scope.saveService = function() {
                $scope.saveButton.state = 'in-progress';
                OrgSvcs.update({ organizationId: params.org, entityType: 'services', entityId:params.service, versionsOrActivity: 'versions', version: params.version }, $scope.updatedService, function(reply) {
                    $scope.isDirty = false;
                    $scope.saveButton.state = 'complete';
                    $scope.version = reply;
                    EntityStatusService.setEntityStatus(reply.status);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('ServiceImpl', pageData, $scope, function() {
                $scope.apiSecurity = toApiSecurity($scope.version);
                $scope.reset();
                PageLifecycle.setPageTitle('service-impl', [ $scope.service.name ]);
            });
        }])

}

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

                if (!$scope.updatedApi.endpoint) {
                    valid = false;
                }

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

                    if (newValue.parsePayload != $scope.version.parsePayload) {
                        dirty = true;
                    }

                    if (newValue.disableKeysStrip != $scope.version.disableKeysStrip)
                    {
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

            $scope.$watch('apiSecurity', function(newValue) {
                if (newValue) {
                    $scope.updatedApi.endpointProperties = toEndpointProperties(newValue);
                    $scope.checkValid();
                }
            }, true);


            // Used, as you'd guess, to compare originally selected values to new values
            function arraysAreEqual(one, two) {
                return (one.join('') == two.join(''));
            }

            $scope.$watch('selectedGateways', function(newValue) {
                if (newValue) {
                    var alreadySet = false;
                    var newSelectedArray = [];

                    // Iterate over each selected value, push into an empty array with proper formatting
                    for(var i = 0; i < newValue.length; i++) {
                        newSelectedArray.push({gatewayId: newValue[i].id});
                    }

                    // Will need to compare newly selected gateways to available gateways again
                    // by plucking gatewayId values, inserting into an array, and comparing them
                    var pluckedAfter = _.map(newSelectedArray, 'gatewayId');
                    var pluckedBefore = _.map($scope.version.gateways, 'gatewayId');

                    var compare = arraysAreEqual(pluckedAfter, pluckedBefore);

                    if(compare === true) {
                        alreadySet = true;
                    }

                    if (!alreadySet) {
                        $scope.updatedApi.gateways = newSelectedArray;
                    } else {
                        delete $scope.updatedApi.gateways;
                    }
                }
            });

            $scope.setEndpointProperties = function(newValue) {
                if (newValue) {
                    $scope.updatedApi.endpointProperties = toEndpointProperties(newValue);
                    $scope.checkValid();
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
                $scope.updatedApi.parsePayload = $scope.version.parsePayload;
                $scope.updatedApi.disableKeysStrip = $scope.version.disableKeysStrip;


                // Gateway Handling

                delete $scope.updatedApi.gateways;
                $scope.selectedGateways = [];

                // Match up currently associated gateways for this API with available gateways
                // to provide additional information, other than just the ID, about each gateway

                if ($scope.version.gateways && $scope.version.gateways.length > 0) {
                    for(var i = 0; i < $scope.gateways.length; i++) {
                        for(var j = 0; j < $scope.version.gateways.length; j++) {
                            // Check if IDs match
                            if($scope.gateways[i].id === $scope.version.gateways[j].gatewayId) {
                                // Add gateway to selected gateway array
                                $scope.selectedGateways.push($scope.gateways[i]);
                            }
                        }
                    }
                }

                $rootScope.isDirty = false;

                // Automatically set the selected gateway if there's only one and the
                // gateway is not already set.
                if ((!$scope.version.gateways || $scope.version.gateways.length == 0)
                    && ($scope.gateways && $scope.gateways.length === 1)) {
                    $scope.autoGateway = true;
                    $scope.selectedGateways[0] = $scope.gateways[0];
                }
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

                // $scope.gateways - list of available gateways
                // $scope.version.gateways - list of gateways for this specific version
                // $scope.selectedGateway - list of currently selected gateways (with `name`, `ID`, etc.)
                // $scope.updatedApi.gateways - what we submit to the API, with only the `gatewayId`
            });
        }]);
}

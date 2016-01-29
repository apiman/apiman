/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewContractController = _module.controller("Apiman.NewContractController",
        ['$q', '$location', '$scope', 'OrgSvcs', 'CurrentUserSvcs', 'PageLifecycle', 'Logger', '$rootScope', 'Dialogs',
        ($q, $location, $scope, OrgSvcs, CurrentUserSvcs, PageLifecycle, Logger, $rootScope, Dialogs) => {
            var params = $location.search();
            var apiId = params.api;
            var apiOrgId = params.apiorg;
            var apiVer = params.apiv;
            var planId = params.planid;
            $scope.saving = false;
            $scope.selectedClientVersion = null;
            
            $scope.refreshClientVersions = function(organizationId, clientId, onSuccess, onError) {
                OrgSvcs.query({ organizationId: organizationId, entityType: 'clients', entityId: clientId, versionsOrActivity: 'versions' }, function(versions) {
                    var plainVersions = [];

                    angular.forEach(versions, function(version) {
                        if (version.status == 'Created' || version.status == 'Ready' || version.status == 'Registered') {
                            plainVersions.push(version.version);
                        }
                    });

                    $scope.clientVersions = plainVersions;

                    if (onSuccess) {
                        onSuccess(plainVersions);
                    }
                }, PageLifecycle.handleError);
            };
            
            var pageData = {
                clients: $q(function(resolve, reject) {
                    CurrentUserSvcs.query({ what: 'clients' }, function(clients) {
                        Logger.info("clients: {0}", clients);
                        if ($rootScope.mruClient) {
                            for (var i = 0; i < clients.length; i++) {
                                var client = clients[i];
                                if (client.organizationId == $rootScope.mruClient.client.organization.id && client.id == $rootScope.mruClient.client.id) {
                                    $scope.selectedClient = client;
                                }
                            }
                        } else if (clients) {
                            $scope.selectedClient = clients[0];
                        } else {
                            $scope.selectedClient = undefined;
                        }

                        $scope.changedClient($scope.selectedClient);

                        resolve(clients);
                    }, reject);
                }),
                selectedApi: $q(function(resolve, reject) {
                    if (apiId && apiOrgId && apiVer) {
                        Logger.debug('Loading api {0}/{1} version {2}.', apiOrgId, apiId, apiVer);

                        OrgSvcs.get({ organizationId: apiOrgId, entityType: 'apis', entityId: apiId, versionsOrActivity: 'versions', version: apiVer }, function(apiVersion) {
                            apiVersion.organizationName = apiVersion.api.organization.name;
                            apiVersion.organizationId = apiVersion.api.organization.id;
                            apiVersion.name = apiVersion.api.name;
                            apiVersion.id = apiVersion.api.id;
                            resolve(apiVersion);
                        }, reject);
                    } else {
                        resolve(undefined);
                    }
                })
            };

            $scope.changedClient = function(newValue) {
                Logger.debug("Client App selected: {0}", newValue);

                $scope.clientVersions = [];

                $scope.selectedClient = newValue;

                $scope.refreshClientVersions(newValue.organizationId, newValue.id, function(versions) {
                    Logger.debug("Versions: {0}", versions);

                    if ($rootScope.mruClient) {
                        if ($rootScope.mruClient.client.organization.id == newValue.organizationId && $rootScope.mruClient.client.id == newValue.id) {
                            $scope.selectedClientVersion = $rootScope.mruClient.version;
                        }
                    } else {
                        if (versions.length > 0) {
                            $scope.selectedClientVersion = versions[0];
                        }
                    }
                });
            };
            
            $scope.selectApi = function() {
                Dialogs.selectApi('Select an API', function(apiVersion) {
                    $scope.selectedApi = apiVersion;
                }, true);
            };

            $scope.changedPlan = function(newValue) {
                $scope.selectedPlan = newValue;
            };

            $scope.$watch('selectedApi', function(newValue) {
                if (!newValue) {
                    $scope.plans = undefined;
                    $scope.selectedPlan = undefined;

                    return;
                }

                Logger.debug('Api selection made, fetching plans.');

                OrgSvcs.query({ organizationId: newValue.organizationId, entityType: 'apis', entityId: newValue.id, versionsOrActivity: 'versions', version: newValue.version, policiesOrActivity: 'plans' }, function(plans) {
                    $scope.plans = plans;
                    Logger.debug("Found {0} plans: {1}.", plans.length, plans);

                    if (plans.length > 0) {
                        if (planId) {
                            for (var i = 0; i < plans.length; i++) {
                                if (plans[i].planId == planId) {
                                    $scope.selectedPlan = plans[i];
                                }
                            }
                        } else {
                            $scope.selectedPlan = undefined;

                            if(plans.length > 0) {
                                $scope.selectedPlan = plans[0];
                            }
                        }
                    } else {
                        $scope.plans = undefined;
                    }
                }, PageLifecycle.handleError);
            });

            $scope.isDisabled = function() {
                return (!$scope.selectedClient || !$scope.selectedClientVersion || !$scope.selectedPlan || !$scope.selectedApi);
            };

            $scope.createContract = function() {
                Logger.log("Creating new contract from {0}/{1} ({2}) to {3}/{4} ({5}) through the {6} plan!", 
                        $scope.selectedClient.organizationName, $scope.selectedClient.name, $scope.selectedClientVersion,
                        $scope.selectedApi.organizationName, $scope.selectedApi.name, $scope.selectedApi.version,
                        $scope.selectedPlan.planName);

                $scope.createButton.state = 'in-progress';

                var newContract = {
                    apiOrgId : $scope.selectedApi.organizationId,
                    apiId : $scope.selectedApi.id,
                    apiVersion : $scope.selectedApi.version,
                    planId : $scope.selectedPlan.planId
                };

                OrgSvcs.save({ organizationId: $scope.selectedClient.organizationId, entityType: 'clients', entityId: $scope.selectedClient.id, versionsOrActivity: 'versions', version: $scope.selectedClientVersion, policiesOrActivity: 'contracts' }, newContract, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/clients/{1}/{2}/contracts', $scope.selectedClient.organizationId, $scope.selectedClient.id, $scope.selectedClientVersion);
                }, PageLifecycle.handleError);
            };
            
            PageLifecycle.loadPage('NewContract', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('new-contract');
            });
        }]);
}

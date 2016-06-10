/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ClientRedirectController = _module.controller('Apiman.ClientRedirectController',
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams) => {
            var orgId = $routeParams.org;
            var clientId = $routeParams.client;

            var pageData = {
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'clients', entityId: clientId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };

            $scope.organizationId = orgId;

            PageLifecycle.loadPage('ClientRedirect', 'clientView', pageData, $scope, function() {
                var version = $scope.versions[0].version;

                if (!version) {
                    PageLifecycle.handleError({ status: 404 });
                } else {
                    PageLifecycle.forwardTo('/orgs/{0}/clients/{1}/{2}', orgId, clientId, version);
                }
            });
        }]);

    export var ClientEntityLoader = _module.factory('ClientEntityLoader',
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams', 'EntityStatusSvc',
        ($q, OrgSvcs, Logger, $rootScope, $routeParams, EntityStatusSvc) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $routeParams;

                    //console.log('params provided to ClientEntityLoader: ' + JSON.stringify(params));

                    return {
                        version: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version }, function(version) {
                                $scope.org = version.client.organization;
                                $scope.client = version.client;
                                $rootScope.mruClient = version;
                                EntityStatusSvc.setEntity(version, 'client');
                                Logger.debug('client version: {0}', version);
                                resolve(version);
                            }, reject);
                        }),
                        versions: $q(function(resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions' }, resolve, reject);
                        })
                    };
                }
            }
        }]);

    export var ClientEntityController = _module.controller('Apiman.ClientEntityController',
        [
            '$q',
            '$uibModal',
            '$scope',
            '$rootScope',
            '$location',
            'ActionSvcs',
            'Logger',
            'PageLifecycle',
            '$routeParams',
            'OrgSvcs',
            'EntityStatusSvc',
            'Configuration',
        ($q, $uibModal, $scope, $rootScope, $location, ActionSvcs, Logger, PageLifecycle, $routeParams, OrgSvcs, EntityStatusSvc, Configuration) => {
            var params = $routeParams;

            $scope.setEntityStatus = EntityStatusSvc.setEntityStatus;
            $scope.getEntityStatus = EntityStatusSvc.getEntityStatus;
            $scope.showMetrics = Configuration.ui.metrics;

            $scope.setVersion = function(client) {
                PageLifecycle.redirectTo('/orgs/{0}/clients/{1}/{2}', params.org, params.client, client.version);
            };
            
            $scope.isModified = function() {
                if (!$scope.version.publishedOn) {
                    return false;
                }
                var pub = new Date($scope.version.publishedOn);
                var mod = new Date($scope.version.modifiedOn);
                return mod > pub;
            };
            
            $scope.isReregisterable = function() {
                var rval = false;
                if ($scope.getEntityStatus() == 'Retired') {
                    Logger.info('Entity is retired, so it **CAN** be re-registered.');
                    rval = true;
                }
                if ($scope.getEntityStatus() == 'Registered') {
                    var mod = $scope.isModified();
                    rval = mod;
                }
                return rval;
            };

            $scope.registerClient = function() {
                $scope.registerButton.state = 'in-progress';
                $scope.reregisterButton.state = 'in-progress';
                
                var registerAction = {
                    type: 'registerClient',
                    entityId: params.client,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                
                ActionSvcs.save(registerAction, function(reply) {
                    $scope.version.publishedOn = Date.now();
                    $scope.registerButton.state = 'complete';
                    $scope.reregisterButton.state = 'complete';
                    $scope.setEntityStatus('Registered');
                    Logger.info('---');
                }, PageLifecycle.handleError);
            };

            $scope.unregisterClient = function(size) {
                $scope.unregisterButton.state = 'in-progress';

                var options = {
                    publishedOnly: true,
                    title: 'Confirm Unregister App'
                };

                $scope.animationsEnabled = true;

                $scope.toggleAnimation = function () {
                    $scope.animationsEnabled = !$scope.animationsEnabled;
                };

                var modalInstance = $uibModal.open({
                    animation: $scope.animationsEnabled,
                    templateUrl: 'confirmModal.html',
                    //templateUrl: 'modal.html',
                    controller: 'ModalConfirmCtrl',
                    size: size,
                    resolve: {
                        options: function () {
                            return options;
                        }
                    }
                });

                modalInstance.result.then(function () {
                    var unregisterAction = {
                        type: 'unregisterClient',
                        entityId: params.client,
                        organizationId: params.org,
                        entityVersion: params.version
                    };

                    ActionSvcs.save(unregisterAction, function(reply) {
                        $scope.version.status = 'Retired';
                        $scope.unregisterButton.state = 'complete';
                        $scope.setEntityStatus($scope.version.status);
                    }, PageLifecycle.handleError);
                }, function () {
                    //console.log('Modal dismissed at: ' + new Date());
                    $scope.unregisterButton.state = 'complete';
                });

            };

            $scope.updateClientDescription = function(updatedDescription) {
                var updateClientBean = {
                    description: updatedDescription
                };

                OrgSvcs.update({
                    organizationId: $scope.organizationId,
                    entityType: 'clients',
                    entityId: $scope.client.id
                },
                updateClientBean,
                function(success) {},
                function(error) {
                    Logger.error('Unable to update client description: {0}', error);
                });
            };



            // ----- Delete --------------------->>>>


            // Add check for ability to delete, show/hide Delete option
            $scope.canDelete = function() {};

            // Call delete, open modal
            $scope.callDelete = function(size) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'deleteModal.html',
                    controller: 'ModalClientAppDeleteCtrl',
                    size: size,
                    resolve: {
                        client: function() {
                            return $scope.client;
                        },
                        params: function() {
                            return params;
                        }
                    }
                });

                modalInstance.result.then(function (selectedItem) {
                    $scope.selected = selectedItem;
                }, function () {
                    Logger.info('Modal dismissed at: ' + new Date());
                });
            };
    }]);

}

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ClientRedirectController = _module.controller("Apiman.ClientRedirectController",
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
                    console.log('params provided to ClientEntityLoader: ' + JSON.stringify(params));
                    return {
                        version: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'clients', entityId: params.client, versionsOrActivity: 'versions', version: params.version }, function(version) {
                                $scope.org = version.client.organization;
                                $scope.client = version.client;
                                $rootScope.mruClient = version;
                                EntityStatusSvc.setEntity(version, 'client');
                                Logger.debug("client version: {0}", version);
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

    export var ClientEntityController = _module.controller("Apiman.ClientEntityController",
        ['$q', '$scope', '$location', 'ActionSvcs', 'Logger', 'Dialogs', 'PageLifecycle', '$routeParams', 'OrgSvcs', 'EntityStatusSvc', 'Configuration',
        ($q, $scope, $location, ActionSvcs, Logger, Dialogs, PageLifecycle, $routeParams, OrgSvcs, EntityStatusSvc, Configuration) => {
            var params = $routeParams;

            $scope.setEntityStatus = EntityStatusSvc.setEntityStatus;
            $scope.getEntityStatus = EntityStatusSvc.getEntityStatus;
            $scope.showMetrics = Configuration.ui.metrics;

            $scope.setVersion = function(client) {
                PageLifecycle.redirectTo('/orgs/{0}/clients/{1}/{2}', params.org, params.client, client.version);
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
                    $scope.version.status = 'Registered';
                    Logger.info("OLD PUBLISHED-ON VALUE: {0}", $scope.version.publishedOn);
                    $scope.version.publishedOn = Date.now();
                    Logger.info("NEW PUBLISHED-ON VALUE: {0}", $scope.version.publishedOn);
                    $scope.registerButton.state = 'complete';
                    $scope.reregisterButton.state = 'complete';
                    $scope.setEntityStatus($scope.version.status);
                }, PageLifecycle.handleError);
            };

            $scope.unregisterClient = function() {
                $scope.unregisterButton.state = 'in-progress';
                Dialogs.confirm('Confirm Unregister App', 'Do you really want to unregister the Client App?  This cannot be undone.', function() {
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
                }, function() {
                    $scope.unregisterButton.state = 'complete';
                });
            };

            $scope.updateClientDescription = function(updatedDescription) {
                var updateClientBean = {
                    description: updatedDescription
                }

                OrgSvcs.update({
                    organizationId: $scope.organizationId,
                    entityType: 'clients',
                    entityId: $scope.client.id
                },
                updateClientBean,
                function(success) {
                },
                function(error) {
                    Logger.error("Unable to update client description: {0}", error);
                });
            };

        }])

}

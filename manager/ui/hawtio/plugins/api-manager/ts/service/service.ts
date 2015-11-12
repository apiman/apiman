/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ServiceRedirectController = _module.controller("Apiman.ServiceRedirectController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams) => {
            var orgId = $routeParams.org;
            var serviceId = $routeParams.service;

            var pageData = {
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'services', entityId: serviceId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };

            PageLifecycle.loadPage('ServiceRedirect', pageData, $scope, function() {
                var version = $scope.versions[0].version;
                if (!version) {
                    PageLifecycle.handleError({ status: 404 });
                } else {
                    PageLifecycle.forwardTo('/orgs/{0}/services/{1}/{2}', orgId, serviceId, version);
                }
            });
        }]);

    export var ServiceEntityLoader = _module.factory('ServiceEntityLoader',
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams', 'EntityStatusService',
        ($q, OrgSvcs, Logger, $rootScope, $routeParams, EntityStatusService) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $routeParams;

                    return {
                        version: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions', version: params.version }, function(version) {
                                $scope.org = version.service.organization;
                                $scope.service = version.service;
                                $rootScope.mruService = version;
                                console.log('version.status: ' + version.status);
                                EntityStatusService.setEntityStatus(version.status);
                                resolve(version);
                            }, reject);
                        }),
                        versions: $q(function(resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'services', entityId: params.service, versionsOrActivity: 'versions' }, resolve, reject);
                        })
                    };
                }
            }
        }]);

    export var ServiceEntityController = _module.controller("Apiman.ServiceEntityController",
        ['$q', '$scope', 'ActionSvcs', 'Logger', 'Dialogs', 'PageLifecycle', '$routeParams', 'OrgSvcs', 'EntityStatusService', 'Configuration',
        ($q, $scope, ActionSvcs, Logger, Dialogs, PageLifecycle, $routeParams, OrgSvcs, EntityStatusService, Configuration) => {
            var params = $routeParams;
            $scope.params = params;

            $scope.setEntityStatus = function(status) {
                EntityStatusService.setEntityStatus(status);
            };

            $scope.getEntityStatus = function() {
                return EntityStatusService.getEntityStatus();
            };

            $scope.isEntityDisabled = function() {
                var status = EntityStatusService.getEntityStatus();

                return (status !== 'Created' && status !== 'Ready');
            };


            // ----- Status Checklist Popover --------------------->>>>

            $scope.checklist = [];

            // Initiates the tooltip (this is required for performance reasons)
            $(function () {
                $('[data-toggle="popover"]').popover();
            });


            // Set initial popover value to be closed
            $scope.isOpen = false;

            // Set initial collapse (for context information in checklist) to be closed
            $scope.isCollapsed = true;

            // Popover options
            $scope.checklistPopover = {
                templateUrl: 'checklistTemplate.html',
                title: 'Checklist'
            };

            // Called when user clicks 'Why can't I publish?' & opens modal
            $scope.getStatusDetails = function() {
                return OrgSvcs.get({
                    organizationId: params.org,
                    entityType: 'services',
                    entityId: params.service,
                    versionsOrActivity: 'versions',
                    version: params.version,
                    policiesOrActivity: 'status'
                }, function(response) {
                    $scope.checklist = response.items;

                    $scope.isOpen = true;
                });
            };


            // ----- Service Update Methods --------------------->>>>

            $scope.setVersion = function(service) {
                PageLifecycle.redirectTo('/orgs/{0}/services/{1}/{2}', params.org, params.service, service.version);
            };

            $scope.publishService = function() {
                $scope.publishButton.state = 'in-progress';

                var publishAction = {
                    type: 'publishService',
                    entityId: params.service,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                
                ActionSvcs.save(publishAction, function(reply) {
                    $scope.version.status = 'Published';
                    $scope.publishButton.state = 'complete';
                    $scope.setEntityStatus($scope.version.status);
                }, PageLifecycle.handleError);
            };

            $scope.retireService = function() {
                $scope.retireButton.state = 'in-progress';

                Dialogs.confirm('Confirm Retire Service', 'Do you really want to retire this service?  This action cannot be undone.', function() {
                    var retireAction = {
                        type: 'retireService',
                        entityId: params.service,
                        organizationId: params.org,
                        entityVersion: params.version
                    };

                    ActionSvcs.save(retireAction, function(reply) {
                        $scope.version.status = 'Retired';
                        $scope.retireButton.state = 'complete';
                        $scope.setEntityStatus($scope.version.status);
                    }, PageLifecycle.handleError);

                }, function() {
                    $scope.retireButton.state = 'complete';
                });
            };

            $scope.updateServiceDescription = function(updatedDescription) {
                var updateServiceBean = {
                    description: updatedDescription
                };

                OrgSvcs.update({
                    organizationId: $scope.organizationId,
                    entityType: 'services',
                    entityId: $scope.service.id,
                },
                updateServiceBean,
                function(success) {
                    Logger.info("Updated sucessfully");
                },
                function(error) {
                    Logger.error("Unable to update service description:  {0}", error);
                });
            };
        }]);
}

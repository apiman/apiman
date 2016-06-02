/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ApiRedirectController = _module.controller("Apiman.ApiRedirectController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'CurrentUser', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, CurrentUser, $routeParams) => {
            var orgId = $routeParams.org;
            var apiId = $routeParams.api;

            var pageData = {
                versions: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: orgId, entityType: 'apis', entityId: apiId, versionsOrActivity: 'versions' }, resolve, reject);
                })
            };
            $scope.organizationId = orgId;

            PageLifecycle.loadPage('ApiRedirect', 'apiView', pageData, $scope, function() {
                var version = $scope.versions[0].version;
                if (!version) {
                    PageLifecycle.handleError({ status: 404 });
                } else {
                    PageLifecycle.forwardTo('/orgs/{0}/apis/{1}/{2}', orgId, apiId, version);
                }
            });
        }]);

    export var ApiEntityLoader = _module.factory('ApiEntityLoader',
        ['$q', 'OrgSvcs', 'Logger', '$rootScope', '$routeParams', 'EntityStatusSvc',
        ($q, OrgSvcs, Logger, $rootScope, $routeParams, EntityStatusSvc) => {
            return {
                getCommonData: function($scope, $location) {
                    var params = $routeParams;

                    return {
                        version: $q(function(resolve, reject) {
                            OrgSvcs.get({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions', version: params.version }, function(version) {
                                $scope.org = version.api.organization;
                                $scope.api = version.api;
                                $rootScope.mruApi = version;
                                EntityStatusSvc.setEntity(version, 'api');
                                resolve(version);
                            }, reject);
                        }),
                        versions: $q(function(resolve, reject) {
                            OrgSvcs.query({ organizationId: params.org, entityType: 'apis', entityId: params.api, versionsOrActivity: 'versions' }, resolve, reject);
                        })
                    };
                }
            }
        }]);

    export var ApiEntityController = _module.controller("Apiman.ApiEntityController",
        ['$rootScope', '$q', '$location', '$scope', '$uibModal', 'ActionSvcs', 'Logger', 'Dialogs', 'PageLifecycle', '$routeParams', 'OrgSvcs', 'EntityStatusSvc', 'Configuration',
        ($rootScope, $q, $location, $scope, $uibModal, ActionSvcs, Logger, Dialogs, PageLifecycle, $routeParams, OrgSvcs, EntityStatusSvc, Configuration) => {
            var params = $routeParams;
            $scope.params = params;

            $scope.setEntityStatus = EntityStatusSvc.setEntityStatus;
            $scope.getEntityStatus = EntityStatusSvc.getEntityStatus;

            $scope.isEntityDisabled = EntityStatusSvc.isEntityDisabled;

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
                title: 'Publish Checklist'
            };

            // Programmatically close popover
            $scope.closePopover = function() {
                $scope.isOpen = false;
            };


            // ----- Delete --------------------->>>>
            $scope.showCtxMenu = true;
            if (Configuration.ui.platform == 'f8' || Configuration.ui.platform == 'ose') {
                $scope.showCtxMenu = false;
            }
            
            // Add check for ability to delete, show/hide Delete option
            $scope.canDelete = function() {};

            // Call delete, open modal
            $scope.callDelete = function(size) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'deleteModal.html',
                    controller: 'ApiDeleteModalCtrl',
                    size: size,
                    resolve: {
                        api: function() {
                            return $scope.api;
                        },
                        params: function() {
                            return $scope.params;
                        }
                    }
                });

                modalInstance.result.then(function (selectedItem) {
                    $scope.selected = selectedItem;
                }, function () {
                    Logger.info('Modal dismissed at: ' + new Date());
                });
            };



            // ----- Why Can't I Publish? --------------------->>>>


            // Called when user clicks 'Why can't I publish?' & opens modal
            $scope.getStatusDetails = function() {
                if($scope.isOpen === false) {
                    return OrgSvcs.get({
                        organizationId: params.org,
                        entityType: 'apis',
                        entityId: params.api,
                        versionsOrActivity: 'versions',
                        version: params.version,
                        policiesOrActivity: 'status'
                    }, function(response) {
                        $scope.checklist = response.items;

                        $scope.isOpen = true;
                    });
                } else if($scope.isOpen === true) {
                    $scope.isOpen = false;
                }
            };

            $scope.oneAtATime = true;

            $scope.goTo = function(idx, item, e) {
                if (e) {
                    e.preventDefault();
                    e.stopPropagation();
                }

                $location.path( $rootScope.pluginName + '/orgs/' + params.org + '/apis/' + params.api + '/' + params.version + '/' + item.path);
            };


            // ----- Api Update Methods --------------------->>>>

            $scope.setVersion = function(api) {
                PageLifecycle.redirectTo('/orgs/{0}/apis/{1}/{2}', params.org, params.api, api.version);
            };

            $scope.isModified = function() {
                if (!$scope.version.publishedOn) {
                    return false;
                }
                var pub = new Date($scope.version.publishedOn);
                var mod = new Date($scope.version.modifiedOn);
                return mod > pub;
            };

            $scope.publishApi = function() {
                $scope.publishButton.state = 'in-progress';
                $scope.republishButton.state = 'in-progress';

                var publishAction = {
                    type: 'publishAPI',
                    entityId: params.api,
                    organizationId: params.org,
                    entityVersion: params.version
                };
                
                ActionSvcs.save(publishAction, function(reply) {
                    $scope.version.publishedOn = Date.now();
                    $scope.publishButton.state = 'complete';
                    $scope.republishButton.state = 'complete';
                    $scope.setEntityStatus('Published');
                }, PageLifecycle.handleError);
            };

            $scope.retireApi = function() {
                $scope.retireButton.state = 'in-progress';

                Dialogs.confirm('Confirm Retire API', 'Do you really want to retire this API?  This action cannot be undone.', function() {
                    var retireAction = {
                        type: 'retireAPI',
                        entityId: params.api,
                        organizationId: params.org,
                        entityVersion: params.version
                    };

                    ActionSvcs.save(retireAction, function(reply) {
                        $scope.version.status = 'Retired';
                        $scope.version.retiredOn = Date.now();
                        $scope.retireButton.state = 'complete';
                        $scope.setEntityStatus($scope.version.status);
                    }, PageLifecycle.handleError);

                }, function() {
                    $scope.retireButton.state = 'complete';
                });
            };

            $scope.updateApiDescription = function(updatedDescription) {
                var updateApiBean = {
                    description: updatedDescription
                };

                OrgSvcs.update({
                    organizationId: $scope.organizationId,
                    entityType: 'apis',
                    entityId: $scope.api.id,
                },
                updateApiBean,
                function(success) {
                    Logger.info("Updated sucessfully");
                },
                function(error) {
                    Logger.error("Unable to update API description:  {0}", error);
                });
            };
        }]);


    export var ApiDeleteModalCtrl = _module.controller('ApiDeleteModalCtrl', function ($location,
                                                                                       $rootScope,
                                                                                       $scope,
                                                                                       $uibModalInstance,
                                                                                       ApiSvcs,
                                                                                       Configuration,
                                                                                       PageLifecycle,
                                                                                       api,
                                                                                       params) {

        $scope.api = api;
        $scope.params = params;

        $scope.confirmApiName = '';

        // Used for enabling/disabling the submit button
        $scope.okayToDelete = false;

        $scope.typed = function () {
            // For user convenience, compare lower case values so that check is not case-sensitive
            $scope.okayToDelete = ($scope.confirmApiName.toLowerCase() === api.name.toLowerCase());
        };

        // Yes, delete the API
        $scope.yes = function () {
            var deleteAction = {
                apiId: params.api,
                orgId: params.org
            };

            ApiSvcs.deleteApi(deleteAction).then(function(res) {
                $scope.okayToDelete = false;

                setTimeout(function() {
                    $uibModalInstance.close();

                    // Redirect users to their list of APIs
                    $location.path($rootScope.pluginName + '/users/' + Configuration.user.username + '/apis');
                }, 800);

                // We should display some type of Toastr/Growl notification to the user here
            }, function(err) {
                $scope.okayToDelete = false;
                $uibModalInstance.close();
                PageLifecycle.handleError(err);
            });
        };

        // No, do NOT delete the API
        $scope.no = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
}

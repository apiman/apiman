/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>

module Apiman {
    export var OrgSidebarController = _module.controller("Apiman.OrgSidebarController",
    ['Logger', '$uibModal', '$scope', 'OrgSvcs', 'Configuration', (Logger, $uibModal, $scope, OrgSvcs, Configuration) => {
        $scope.updateOrgDescription = function(updatedDescription) {
            var updateOrganizationBean = {
                description: updatedDescription
            };

            OrgSvcs.update({ organizationId: $scope.organizationId },
                updateOrganizationBean,
                function(success) {
                },
                function(error) {
                    Logger.error("Unable to update org description: {0}", error);
                });
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
                controller: 'OrgDeleteModalCtrl',
                size: size,
                resolve: {
                    org: function() {
                        return $scope.org;
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

    export var OrgDeleteModalCtrl = _module.controller('OrgDeleteModalCtrl', function ($location,
                                                                                       $rootScope,
                                                                                       $scope,
                                                                                       $uibModalInstance,
                                                                                       OrgSvcs,
                                                                                       Configuration,
                                                                                       PageLifecycle,
                                                                                       org) {

        $scope.confirmOrgName = '';
        $scope.org = org;

        // Used for enabling/disabling the submit button
        $scope.okayToDelete = false;

        $scope.typed = function () {
            // For user convenience, compare lower case values so that check is not case-sensitive
            $scope.okayToDelete = ($scope.confirmOrgName.toLowerCase() === org.name.toLowerCase());
        };

        // Yes, delete the organization
        $scope.yes = function () {
            var deleteAction = {
                organizationId: org.id
            };

            OrgSvcs.remove(deleteAction).$promise.then(function(res) {
                $scope.okayToDelete = false;

                setTimeout(function() {
                    $uibModalInstance.close();

                    // Redirect users to their list of organizations
                    $location.path($rootScope.pluginName + '/users/' + Configuration.user.username + '/orgs');
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

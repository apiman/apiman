/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>

module Apiman {
    export var OrgSidebarController = _module.controller("Apiman.OrgSidebarController",
    ['Logger', '$scope', 'OrgSvcs', (Logger, $scope, OrgSvcs) => {
        $scope.updateOrgDescription = function(updatedDescription) {
            var updateOrganizationBean = {
                description: updatedDescription
            }

            OrgSvcs.update({ organizationId: $scope.organizationId },
                updateOrganizationBean,
                function(success) {
                },
                function(error) {
                    Logger.error("Unable to update org description: {0}", error);
                });
        }
    }]);
}

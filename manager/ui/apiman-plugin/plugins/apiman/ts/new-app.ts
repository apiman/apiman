/// <reference path="apimanPlugin.ts"/>
module Apiman {

    export var NewAppController = _module.controller("Apiman.NewAppController",
        ['$q', '$location', '$scope', 'UserSvcs', 'OrgSvcs', 'PageLifecycle', ($q, $location, $scope, UserSvcs, OrgSvcs, PageLifecycle) => {
            var promise = $q.all({
                organizations: $q(function(resolve, reject) {
                    UserSvcs.query({ entityType: 'organizations' }, function(userOrgs) {
                        $scope.selectedOrg = userOrgs[0];
                        resolve(userOrgs);
                    }, function(error) {
                        reject(error);
                    });
                }),
            });

            $scope.setOrg = function(org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewApp = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'applications' }, $scope.app, function(reply) {
                    $location.path(Apiman.pluginName + '/app-overview.html').search('org', $scope.selectedOrg.id).search('app', $scope.app.name).search('version', $scope.app.initialVersion);
                }, function(error) {
                    if (error.status == 409) {
                        $location.path('apiman/error-409.html');
                    } else {
                        $scope.createButton.state = 'error';
                        alert("ERROR=" + error.status + " " + error.statusText);
                    }
                });
            };
            
            PageLifecycle.loadPage('NewApp', promise, $scope);
        }]);

}

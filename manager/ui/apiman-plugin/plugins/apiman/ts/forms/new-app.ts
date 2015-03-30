/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewAppController = _module.controller("Apiman.NewAppController",
        ['$q', '$location', '$scope', 'CurrentUserSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        ($q, $location, $scope, CurrentUserSvcs, OrgSvcs, PageLifecycle, $rootScope) => {
            var recentOrg = $rootScope.mruOrg;

            var promise = $q.all({
                organizations: $q(function(resolve, reject) {
                    CurrentUserSvcs.query({ what: 'apporgs' }, function(orgs) {
                        if (recentOrg) {
                            $scope.selectedOrg = recentOrg;
                        } else if (orgs.length > 0) {
                            $scope.selectedOrg = orgs[0];
                        }
                        resolve(orgs);
                    }, reject);
                }),
            });

            $scope.setOrg = function(org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewApp = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'applications' }, $scope.app, function(reply) {
                    $location.url(Apiman.pluginName + '/app-overview.html').search('org', $scope.selectedOrg.id).search('app', $scope.app.name).search('version', $scope.app.initialVersion);
                }, PageLifecycle.handleError);
            };
            
            $scope.app = {
                initialVersion: '1.0'
            };
            
            PageLifecycle.loadPage('NewApp', promise, $scope, function() {
                PageLifecycle.setPageTitle('new-app');
                $scope.$applyAsync(function() {
                    $('#apiman-entityname').focus();
                });
            });
        }]);

}

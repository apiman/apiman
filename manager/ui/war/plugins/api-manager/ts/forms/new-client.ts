/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewClientController = _module.controller("Apiman.NewClientController",
        ['$q', '$location', '$scope', 'CurrentUserSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        ($q, $location, $scope, CurrentUserSvcs, OrgSvcs, PageLifecycle, $rootScope) => {
            var recentOrg = $rootScope.mruOrg;

            var pageData = {
                organizations: $q(function(resolve, reject) {
                    CurrentUserSvcs.query({ what: 'clientorgs' }, function(orgs) {
                        if (recentOrg) {
                            $scope.selectedOrg = recentOrg;
                        } else if (orgs.length > 0) {
                            $scope.selectedOrg = orgs[0];
                        }
                        resolve(orgs);
                    }, reject);
                }),
            };

            $scope.setOrg = function(org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewClient = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'clients' }, $scope.client, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/clients/{1}/{2}', reply.organization.id, reply.id, $scope.client.initialVersion);
                }, PageLifecycle.handleError);
            };
            
            $scope.client = {
                initialVersion: '1.0'
            };
            
            PageLifecycle.loadPage('NewClient', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('new-client');
                $scope.$applyAsync(function() {
                    $('#apiman-entityname').focus();
                });
            });
        }]);

}

/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewApiController = _module.controller("Apiman.NewApiController",
        ['$q', '$location', '$scope', 'CurrentUser', 'UserSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        ($q, $location, $scope, CurrentUser, UserSvcs, OrgSvcs, PageLifecycle, $rootScope) => {
            var recentOrg = $rootScope.mruOrg;

            var pageData = {
                organizations: $q(function(resolve, reject) {
                    return CurrentUser.getCurrentUser().then(function (currentUser) {
                        return UserSvcs.query({ user: currentUser.username, entityType: 'apiorgs' }, function(orgs) {
                            if (recentOrg) {
                                $scope.selectedOrg = recentOrg;
                            } else if (orgs.length > 0) {
                                $scope.selectedOrg = orgs[0];
                            }
                            resolve(orgs);
                        }, reject);
                    })
                })
            };

            $scope.setOrg = function(org) {
                $scope.selectedOrg = org;
            };
            $scope.saveNewApi = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'apis' }, $scope.api, function(reply) {
                    PageLifecycle.redirectTo('/orgs/{0}/apis/{1}/{2}', reply.organization.id, reply.id, $scope.api.initialVersion);
                }, PageLifecycle.handleError);
            };
            
            $scope.api = {
                initialVersion: '1.0'
            };
            
            PageLifecycle.loadPage('NewApi', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('new-api');
                $scope.$applyAsync(function() {
                    $('#apiman-entityname').focus();
                });
            });
            
        }]);

}

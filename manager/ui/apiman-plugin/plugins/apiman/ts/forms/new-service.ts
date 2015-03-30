/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var NewServiceController = _module.controller("Apiman.NewServiceController",
        ['$q', '$location', '$scope', 'CurrentUserSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        ($q, $location, $scope, CurrentUserSvcs, OrgSvcs, PageLifecycle, $rootScope) => {
            var recentOrg = $rootScope.mruOrg;

            var promise = $q.all({
                organizations: $q(function(resolve, reject) {
                    CurrentUserSvcs.query({ what: 'svcorgs' }, function(orgs) {
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
            $scope.saveNewService = function() {
                $scope.createButton.state = 'in-progress';
                OrgSvcs.save({ organizationId: $scope.selectedOrg.id, entityType: 'services' }, $scope.service, function(reply) {
                    $location.url(Apiman.pluginName + '/service-overview.html').search('org', $scope.selectedOrg.id).search('service', $scope.service.name).search('version', $scope.service.initialVersion);
                }, PageLifecycle.handleError);
            };
            
            $scope.service = {
                initialVersion: '1.0'
            };
            
            PageLifecycle.loadPage('NewService', promise, $scope, function() {
                PageLifecycle.setPageTitle('new-service');
                $scope.$applyAsync(function() {
                    $('#apiman-entityname').focus();
                });
            });
            
        }]);

}

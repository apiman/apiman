/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var ConsumerOrgController = _module.controller("Apiman.ConsumerOrgController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', 'CurrentUser', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, CurrentUser, $routeParams) => {

            $scope.filterApis = function(value) {
                if (!value) {
                    $scope.filteredApis = $scope.apis;
                } else {
                    var filtered = [];
                    angular.forEach($scope.apis, function(api) {
                        if (api.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(api);
                        }
                    });
                    $scope.filteredApis = filtered;
                }
            };
            
            var pageData = {
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: '' }, resolve, reject);
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'members' }, resolve, reject);
                }),
                apis: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'apis' }, resolve, reject);
                })
            };
            
            PageLifecycle.loadPage('ConsumerOrg', undefined, pageData, $scope, function() {
                $scope.org.isMember = CurrentUser.isMember($scope.org.id);
                $scope.filteredApis = $scope.apis;
                PageLifecycle.setPageTitle('consumer-org', [ $scope.org.name ]);
            });
        }])

}

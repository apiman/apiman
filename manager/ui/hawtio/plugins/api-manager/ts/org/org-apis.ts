/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var OrgApisController = _module.controller("Apiman.OrgApisController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams', 'CurrentUser',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams, CurrentUser) => {
            $scope.tab = 'apis';
            var params = $routeParams;
            $scope.organizationId = params.org;

            if (!CurrentUser.hasPermission(params.org, 'apiView')) {
              delete $rootScope['currentUser'];
            }

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
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function(org) {
                        $rootScope.mruOrg = org;
                        resolve(org);
                    }, reject);
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                        resolve(members);
                    }, reject);
                }),
                apis: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'apis' }, function(apis) {
                        $scope.filteredApis = apis;
                        resolve(apis);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgSvcs', 'apiView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('org-apis', [ $scope.org.name ]);
            });
        }])

}

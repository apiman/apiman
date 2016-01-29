/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var UserOrgsController = _module.controller("Apiman.UserOrgsController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, UserSvcs, PageLifecycle, $routeParams) => {
            $scope.tab = 'organizations';

            $scope.filterOrgs = function(value) {
                if (!value) {
                    $scope.filteredOrgs = $scope.organizations;
                } else {
                    var filtered = [];
                    angular.forEach($scope.organizations, function(org) {
                        if (org.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(org);
                        }
                    });
                    $scope.filteredOrgs = filtered;
                }
            };
            
            var pageData = {
                user: $q(function(resolve, reject) {
                    UserSvcs.get({ user: $routeParams.user }, function(user) {
                        if (!user.fullName) {
                            user.fullName = user.username;
                        }
                        resolve(user);
                    }, reject);
                }),
                organizations: $q(function(resolve, reject) {
                    UserSvcs.query({ user: $routeParams.user, entityType: 'organizations' }, function(userOrgs) {
                        $scope.filteredOrgs = userOrgs;
                        resolve(userOrgs);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('UserOrgs', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('user-orgs', [ $scope.user.fullName ]);
            });
    }])

}

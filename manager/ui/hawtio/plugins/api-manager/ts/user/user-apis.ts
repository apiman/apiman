/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var UserApisController = _module.controller("Apiman.UserApisController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, UserSvcs, PageLifecycle, $routeParams) => {
            $scope.tab = 'apis';
            
            $scope.filterApis = function(value) {
                if (!value) {
                    $scope.filteredApis = $scope.apis;
                } else {
                    var filtered = [];
                    angular.forEach($scope.apis, function(api) {
                        if (api.name.toLowerCase().indexOf(value.toLowerCase()) > -1 || api.organizationName.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(api);
                        }
                    });
                    $scope.filteredApis = filtered;
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
                apis: $q(function(resolve, reject) {
                    UserSvcs.query({ user: $routeParams.user, entityType: 'apis' }, function(userApis) {
                        $scope.filteredApis = userApis;
                        resolve(userApis);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('UserApis', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('user-apis', [ $scope.user.fullName ]);
            });
    }])

}

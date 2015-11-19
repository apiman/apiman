/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var UserAppsController = _module.controller("Apiman.UserAppsController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        ($q, $scope, $location, UserSvcs, PageLifecycle, Logger, $routeParams) => {
            $scope.tab = 'applications';
            
            $scope.filterApps = function(value) {
                if (!value) {
                    $scope.filteredApps = $scope.applications;
                } else {
                    var filtered = [];
                    angular.forEach($scope.applications, function(app) {
                        if (app.name.toLowerCase().indexOf(value.toLowerCase()) > -1 || app.organizationName.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(app);
                        }
                    });
                    $scope.filteredApps = filtered;
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
                applications: $q(function(resolve, reject) {
                    UserSvcs.query({ user: $routeParams.user, entityType: 'applications' }, function(userApps) {
                        $scope.filteredApps = userApps;
                        resolve(userApps);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('UserApps', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('user-apps', [ $scope.user.fullName ]);
            });
    }])

}

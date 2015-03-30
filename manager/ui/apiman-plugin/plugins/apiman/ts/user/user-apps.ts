/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var UserAppsController = _module.controller("Apiman.UserAppsController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', 'Logger',
        ($q, $scope, $location, UserSvcs, PageLifecycle, Logger) => {
            var params = $location.search();
            $scope.tab = 'applications';
            
            $scope.filterApps = function(value) {
                if (!value) {
                    $scope.filteredApps = $scope.applications;
                } else {
                    var filtered = [];
                    for (var i = 0; i < $scope.applications.length; i++) {
                        var app = $scope.applications[i];
                        if (app.name.toLowerCase().indexOf(value) > -1 || app.organizationName.toLowerCase().indexOf(value) > -1) {
                            filtered.push(app);
                        }
                    }
                    $scope.filteredApps = filtered;
                }
            };

            var promise = $q.all({
                user: $q(function(resolve, reject) {
                    UserSvcs.get({ user: params.user }, function(user) {
                        if (!user.fullName) {
                            user.fullName = user.username;
                        }
                        resolve(user);
                    }, function(error) {
                        reject(error);
                    });
                }),
                applications: $q(function(resolve, reject) {
                    UserSvcs.query({ user: params.user, entityType: 'applications' }, function(userApps) {
                        $scope.filteredApps = userApps;
                        resolve(userApps);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            PageLifecycle.loadPage('UserApps', promise, $scope, function() {
                PageLifecycle.setPageTitle('user-apps', [ $scope.user.fullName ]);
            });
    }])

}

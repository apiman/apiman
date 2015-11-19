/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var UserServicesController = _module.controller("Apiman.UserServicesController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, UserSvcs, PageLifecycle, $routeParams) => {
            $scope.tab = 'services';
            
            $scope.filterServices = function(value) {
                if (!value) {
                    $scope.filteredServices = $scope.services;
                } else {
                    var filtered = [];
                    angular.forEach($scope.services, function(svc) {
                        if (svc.name.toLowerCase().indexOf(value.toLowerCase()) > -1 || svc.organizationName.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(svc);
                        }
                    });
                    $scope.filteredServices = filtered;
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
                services: $q(function(resolve, reject) {
                    UserSvcs.query({ user: $routeParams.user, entityType: 'services' }, function(userServices) {
                        $scope.filteredServices = userServices;
                        resolve(userServices);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('UserServices', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('user-services', [ $scope.user.fullName ]);
            });
    }])

}

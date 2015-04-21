/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var OrgServicesController = _module.controller("Apiman.OrgServicesController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams) => {
            var params = $routeParams;
            $scope.organizationId = params.org;

            $scope.filterServices = function(value) {
                if (!value) {
                    $scope.filteredServices = $scope.services;
                } else {
                    var filtered = [];
                    angular.forEach($scope.services, function(service) {
                        if (service.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(service);
                        }
                    });
                    $scope.filteredServices = filtered;
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
                services: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services' }, function(services) {
                        $scope.filteredServices = services;
                        resolve(services);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgSvcs', pageData, $scope, function() {
                PageLifecycle.setPageTitle('org-services', [ $scope.org.name ]);
            });
        }])

}

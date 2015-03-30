/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var OrgServicesController = _module.controller("Apiman.OrgServicesController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope) => {
            var params = $location.search();
            $scope.organizationId = params.org;
            
            $scope.filterServices = function(value) {
                if (!value) {
                    $scope.filteredServices = $scope.services;
                } else {
                    var filtered = [];
                    for (var i = 0; i < $scope.services.length; i++) {
                        var service = $scope.services[i];
                        if (service.name.toLowerCase().indexOf(value) > -1) {
                            filtered.push(service);
                        }
                    }
                    $scope.filteredServices = filtered;
                }
            };
            
            var promise = $q.all({
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
            });
            PageLifecycle.loadPage('OrgSvcs', promise, $scope, function() {
                PageLifecycle.setPageTitle('org-services', [ $scope.org.name ]);
            });
        }])

}

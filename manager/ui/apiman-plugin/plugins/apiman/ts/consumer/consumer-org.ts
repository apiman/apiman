/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var ConsumerOrgController = _module.controller("Apiman.ConsumerOrgController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', 'CurrentUser', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, CurrentUser, $routeParams) => {

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
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: '' }, function(org) {
                        org.isMember = CurrentUser.isMember(org.id);
                        resolve(org);
                    }, reject);
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'members' }, function(members) {
                        resolve(members);
                    }, reject);
                }),
                services: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'services' }, function(services) {
                        $scope.filteredServices = services;
                        resolve(services);
                    }, reject);
                })
            });
            
            PageLifecycle.loadPage('ConsumerOrg', promise, $scope, function() {
                PageLifecycle.setPageTitle('consumer-org', [ $scope.org.name ]);
            });
        }])

}

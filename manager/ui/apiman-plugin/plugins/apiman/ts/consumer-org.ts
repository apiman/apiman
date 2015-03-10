/// <reference path="apimanPlugin.ts"/>
/// <reference path="services.ts"/>
module Apiman {

    export var ConsumerOrgController = _module.controller("Apiman.ConsumerOrgController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', ($q, $scope, $location, OrgSvcs, PageLifecycle) => {
            var params = $location.search();
            
            
            
            var promise = $q.all({
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: params.org, entityType: '' }, function(org) {
                        resolve(org);
                    }, function(error) {
                        reject(error);
                    });
                }),
                members: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                        resolve(members);
                    }, function(error) {
                        reject(error);
                    });
                }),
                services: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'services' }, function(services) {
                        resolve(services);
                    }, function(error) {
                        reject(error);
                    });
                })
            });
            
            $scope.filterServices = function() {
                OrgSvcs.query({ organizationId: params.org, entityType: 'services' }, function(services) {
                    var matchedServices = [];
                    if ($scope.serviceName != null) {
                        for (var i = 0; i < services.length ; i++) {
                            var name = services[i].name;
                            var k = services[i].name.indexOf($scope.serviceName);
                            if (services[i].name.indexOf($scope.serviceName) >= 0) {
                                matchedServices.push(services[i]);
                            }                     
                        }
                        $scope.services = matchedServices;
                    } else {
                        $scope.services = services;
                    }
                }, function(error) {
                    alert (error);
                });
                
            }
            
            PageLifecycle.loadPage('ConsumerOrg', promise, $scope);
        }])

}

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var OrgClientsController = _module.controller("Apiman.OrgClientsController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams', 'CurrentUser',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams, CurrentUser) => {
            $scope.tab = 'clients';
            var params = $routeParams;
            $scope.organizationId = params.org;
            
            if (!CurrentUser.hasPermission(params.org, 'clientView')) {
              delete $rootScope['currentUser'];
            }
            
            $scope.filterClients = function(value) {
                if (!value) {
                    $scope.filteredClients = $scope.clients;
                } else {
                    var filtered = [];
                    angular.forEach($scope.clients, function(client) {
                        if (client.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(client);
                        }
                    });
                    $scope.filteredClients = filtered;
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
                clients: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: params.org, entityType: 'clients' }, function(clients) {
                        $scope.filteredClients = clients;
                        resolve(clients);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgClients', 'clientView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('org-clients', [ $scope.org.name ]);
            });
        }])

}

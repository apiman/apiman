/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../rpc.ts"/>
module Apiman {

    export var UserClientsController = _module.controller("Apiman.UserClientsController",
        ['$q', '$scope', '$location', 'UserSvcs', 'PageLifecycle', 'Logger', '$routeParams',
        ($q, $scope, $location, UserSvcs, PageLifecycle, Logger, $routeParams) => {
            $scope.tab = 'clients';
            
            $scope.filterClients = function(value) {
                if (!value) {
                    $scope.filteredClients = $scope.clients;
                } else {
                    var filtered = [];
                    angular.forEach($scope.clients, function(client) {
                        if (client.name.toLowerCase().indexOf(value.toLowerCase()) > -1 || client.organizationName.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(client);
                        }
                    });
                    $scope.filteredClients = filtered;
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
                clients: $q(function(resolve, reject) {
                    UserSvcs.query({ user: $routeParams.user, entityType: 'clients' }, function(userClients) {
                        $scope.filteredClients = userClients;
                        resolve(userClients);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('UserClients', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('user-clients', [ $scope.user.fullName ]);
            });
    }])

}

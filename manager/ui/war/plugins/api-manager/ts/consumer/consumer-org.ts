import {_module} from "../apimanPlugin";
import angular = require("angular");

_module.controller("Apiman.ConsumerOrgController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', 'CurrentUser', '$routeParams',
        function ($q, $scope, $location, OrgSvcs, PageLifecycle, CurrentUser, $routeParams) {

            $scope.filterApis = function(value) {
                if (!value) {
                    $scope.filteredApis = $scope.apis;
                } else {
                    var filtered = [];
                    angular.forEach($scope.apis, function(api) {
                        if (api.name.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(api);
                        }
                    });
                    $scope.filteredApis = filtered;
                }
            };
            
            var pageData = {
                org: $q(function(resolve, reject) {
                    OrgSvcs.get({ organizationId: $routeParams.org, entityType: '' }, resolve, reject);
                }),
                members: $q(function(resolve, reject) {
                    // If we are not a member we don't send this request because it would throw an exception
                    if (CurrentUser.isMember($routeParams.org)){
                        OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'members' }, resolve, reject);
                    } else {
                        resolve();
                    }
                }),
                apis: $q(function(resolve, reject) {
                    OrgSvcs.query({ organizationId: $routeParams.org, entityType: 'apis' }, resolve, reject);
                })
            };
            
            PageLifecycle.loadPage('ConsumerOrg', undefined, pageData, $scope, function() {
                $scope.org.isMember = CurrentUser.isMember($scope.org.id);
                $scope.filteredApis = $scope.apis;
                PageLifecycle.setPageTitle('consumer-org', [ $scope.org.name ]);
            });
        }]);

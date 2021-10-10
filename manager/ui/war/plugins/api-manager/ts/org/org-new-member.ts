// @ts-nocheck
import {_module} from "../apimanPlugin";

_module.controller("Apiman.OrgNewMemberController",
    ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'ApimanSvcs', 'Logger', '$routeParams',
    function ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, ApimanSvcs, $log, $routeParams) {
        var params = $routeParams;
        $scope.organizationId = params.org;
        $scope.selectedUsers = {};
        $scope.selectedRoles = [];
        $scope.queriedUsers = [];
        $scope.searchBoxValue = '';

        $scope.addMembers = function() {
            if ($scope.selectedRoles) {
                $scope.addMembersButton.state = 'in-progress';
                // Iterate over object like map (k:v)
                jQuery.each($scope.selectedUsers, function(k, user) {
                    $log.debug('Adding user: {0}', user);

                    var grantRolesBean = {
                        userId: user.username,
                        roleIds: $scope.selectedRoles
                    };

                    OrgSvcs.save({ organizationId: $scope.organizationId, entityType: 'roles' },
                        grantRolesBean, function() { // Success
                            $log.debug('Successfully Saved: {0}', grantRolesBean);
                            $scope.addMembersButton.state = 'complete';
                            PageLifecycle.redirectTo('/orgs/{0}/manage-members', params.org);
                        }, PageLifecycle.handleError);
                });
            }
        }

        $scope.findUsers = (searchBoxValue) => {
            $scope.searchButton.state = 'in-progress';

            $scope.searchBoxValue = searchBoxValue;
            if (!searchBoxValue || searchBoxValue.length == 0) {
                $scope.queriedUsers = [];
                $scope.searchButton.state = 'complete';
                return;
            }

            let pageSize = 10000; // ES index.max_result_window
            var queryBean = {
                filters: [{
                    name: 'username',
                    value: '*' + searchBoxValue + '*',
                    operator: 'like'
                }],
                orderBy: {
                    name: 'fullName',
                    ascending: true
                },
                paging: {
                    page: 1,
                    pageSize: pageSize
                }
            };

            $log.debug('Query: {0}', queryBean);

            ApimanSvcs.save({ entityType: 'search', secondaryType: 'users' }, queryBean, function(reply) {
                $scope.searchButton.state = 'complete';
                $log.debug('Reply: {0}', reply);
                $scope.queriedUsers = reply.beans;
            }, function() {
                $scope.searchButton.state = 'error';
            });
        }

        $scope.countObjectKeys = (object) => {
            return Object.keys(object).length;
        }

        var pageData = {
            org: $q(function(resolve, reject) {
                OrgSvcs.get({ organizationId: params.org, entityType: '' }, function(org) {
                    $rootScope.mruOrg = org;
                    resolve(org);
                }, reject);
            }),
            members: $q(function(resolve, reject) {
                OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
                    $scope.filteredMembers = members;
                    resolve(members);
                }, reject);
            }),
            roles: $q(function(resolve, reject) {
                ApimanSvcs.query({ entityType: 'roles' }, function(adminRoles) {
                    $scope.filteredRoles = adminRoles;
                    resolve(adminRoles);
                }, reject);
            })
        };

        PageLifecycle.loadPage('OrgNewMember', 'orgAdmin', pageData, $scope, function() {
            PageLifecycle.setPageTitle('new-member');
            $scope.findUsers('*');
        });
    }])

_module.directive('apimanUserEntry', ['Logger', function($log) {
    return {
        scope: {
            user: '=',
            selectedUsers: '='
        },
        replace: true,
        templateUrl: 'plugins/api-manager/html/org/apiman-user-entry.html',
        link: function($scope) {
            $scope.isSelectedUser = false;

            $scope.selectThisUser = function() {
                $scope.isSelectedUser = !$scope.isSelectedUser;
                // If selected user then add to map; if deselected remove it.
                if ($scope.isSelectedUser) {
                    $scope.selectedUsers[$scope.user.username] = $scope.user;
                } else {
                    delete $scope.selectedUsers[$scope.user.username];
                }
                $log.debug("Selected {0}", $scope.user.username);
                $log.debug("Global $scope.selectedUsers {0}", $scope.selectedUsers);
            }
        }
    };
}]);

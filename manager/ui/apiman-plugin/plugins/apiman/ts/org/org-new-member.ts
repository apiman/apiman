/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

  export var OrgNewMemberController = _module.controller("Apiman.OrgNewMemberController",
    ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'ApimanSvcs', 'Logger',
  ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, ApimanSvcs, $log) => {
    var params = $location.search();
    $scope.organizationId = params.org;
    $scope.selectedUsers = {};
    $scope.selectedRoles = [];
    $scope.queriedUsers = [];

    $scope.addMembers = function() {
      if ($scope.selectedRoles) {
        $scope.addMembersButton.state = 'in-progress';
        // Iterate over object like map (k:v)
        jQuery.each($scope.selectedUsers, function(k, user) {
          $log.debug('Adding user: ' + JSON.stringify(user));

          var grantRolesBean = {
            userId: user.username,
            roleIds: $scope.selectedRoles
          };

          OrgSvcs.save({ organizationId: $scope.organizationId, entityType: 'roles' },
            grantRolesBean, function() { // Success
              $log.debug('Successfully Saved: ' + JSON.stringify(grantRolesBean));
              $scope.addMembersButton.state = 'complete';
              $location.url(pluginName + '/org-manage-members.html').search({ org: params.org });
            }, function(error) { // Err TODO handle error better.
              $scope.addMembersButton.state = 'error';
              $log.debug('Error: ' + JSON.stringify(error));
          });
        });
      }
    }

    $scope.findUsers = (searchBoxValue) => {
      $scope.searchBoxValue = searchBoxValue;
      if (searchBoxValue.length == 0) return $scope.queriedUsers = [];

      var queryBean = {
        filters: [{
          name: 'fullName',
          value: '*' + searchBoxValue + '*',
          operator: 'like'
        }],
        orderBy: {
          name: 'fullName',
          ascending: true
        },
        paging: {
          page: 1,
          pageSize: 50
        }
      }

      $log.debug('Query: ' + JSON.stringify(queryBean));

      ApimanSvcs.save({ entityType: 'users', secondaryType: 'search' }, queryBean, function(reply) {
        $log.debug('Reply: ' + JSON.stringify(reply));
        $scope.queriedUsers = reply.beans;
      });
    }

    $scope.countObjectKeys = (object) => {
      return Object.keys(object).length;
    }

    var promise = $q.all({
      org: $q(function(resolve, reject) {
        OrgSvcs.get({ organizationId: params.org, entityType: '' }, function(org) {
          $rootScope.mruOrg = org;
          resolve(org);
        }, function(error) {
          reject(error);
        });
      }),
      members: $q(function(resolve, reject) {
        OrgSvcs.query({ organizationId: params.org, entityType: 'members' }, function(members) {
          $scope.filteredMembers = members;
          resolve(members);
        }, function(error) {
          reject(error);
        });
      }),
      roles: $q(function(resolve, reject) {
        ApimanSvcs.query({ entityType: 'roles' }, function(adminRoles) {
          $scope.filteredRoles = adminRoles;
          resolve(adminRoles);
        }, function(error) {
          reject(error);
        });
      })
    });

    PageLifecycle.loadPage('OrgNewMembers', promise, $scope);
  }])

  OrgMembersController.directive('apimanUserEntry', ['$log', function($log) {
    return {
      scope: {
        user: '=',
        selectedUsers: '='
      },
      templateUrl: 'plugins/apiman/html/org/apiman-user-entry.html',
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
          $log.debug("Selected " + $scope.user.username);
          $log.debug("Global $scope.selectedUsers " + JSON.stringify($scope.selectedUsers));
        }
      }
    };
  }])
}

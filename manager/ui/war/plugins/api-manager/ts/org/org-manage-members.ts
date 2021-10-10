// @ts-nocheck
import {_module} from "../apimanPlugin";
import angular = require("angular");

  export var getRoleIds = function(member) {
    return member.roles.map(function(role) {
      return role.roleId;
    });
  };

  _module.controller("Apiman.OrgManageMembersController",
  ['$q', '$scope', '$location', 'ApimanSvcs', 'OrgSvcs', 'PageLifecycle', '$rootScope', 'Logger', '$routeParams',
    function ($q, $scope, $location, ApimanSvcs, OrgSvcs, PageLifecycle, $rootScope, $log, $routeParams) {
      var params = $routeParams;
      $scope.organizationId = params.org;
      $scope.filteredMembers = [];
      $scope.filterValue = "";
      $scope.selectedRoles = "";

      var containsAnyRoles = function(containsArray) {
        if ($scope.selectedRoles.length === 0) {
          return true;
        }

        var returnVal = false;
        jQuery.each($scope.selectedRoles, function(index, value) {
          if (jQuery.inArray(value, containsArray) > -1) {
            return returnVal = true;
          }
        });
        return returnVal;
      }

      $scope.filterMembers = function(value) {
        $scope.filterValue = value;

        if (!value) {
          // Case 1: no filter value and no selected roles
          // Case 2: no filter value but at least one selected role
          // Case 3:
          if ($scope.selectedRoles.length === 0) {
            $scope.filteredMembers = $scope.members;
          } else {
            $scope.filteredMembers = jQuery.grep($scope.members, function(member: any, _) {
              return containsAnyRoles(getRoleIds(member));
            });
          }
        } else {
          $scope.filteredMembers = jQuery.grep($scope.members, function(m: any, _) {
            return (
              (m.userName.toLowerCase().indexOf(value.toLowerCase()) > -1 || m.userId.toLowerCase().indexOf(value.toLowerCase()) > -1)
              && containsAnyRoles(getRoleIds(m)));
          });
        }
      };

      let pageData = {
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
      PageLifecycle.loadPage('OrgManageMembers', 'orgAdmin', pageData, $scope, function() {
        PageLifecycle.setPageTitle('org-manage-members', [ $scope.org.name ]);
      });
    }]);

_module.directive('apimanUserCard', [
      '$uibModal',
      'OrgSvcs',
      'Logger',
      'PageLifecycle',
    ($uibModal, OrgSvcs, $log, PageLifecycle) => {
      return {
        restrict: 'E',
        scope: {
          member: '=',
          roles: '=',
          orgId: '@'
        },
        template: '<div ng-include="currentTemplate()" ng-show="isCardVisible"></div>',
        link: function($scope, element, attrs) {
          // updatedRoles comes from card-back.
          $scope.updatedRoles = getRoleIds($scope.member);
          $scope.front = 'apiman-user-card-front';
          $scope.back = 'apiman-user-card-back';
          $scope.cardFace = $scope.front;
          $scope.isCardVisible = true;

          $scope.flipCard = function(face) {
            $scope.cardFace = face;
          };

          $scope.currentTemplate = function() {
            return 'plugins/api-manager/html/org/' + $scope.cardFace + '.html';
          };

          $scope.joinRoles = function(roles) {
            return roles.map(function(role) {
              return role.roleName;
            }).join(', ');
          };

          // Update is revoke + grant
          $scope.updateRoles = function(selectedRoles: Array<string>) {
            if (!selectedRoles) return $scope.flipCard($scope.front);

            var grantRolesBean = {
              userId: $scope.member.userId,
              roleIds: selectedRoles
            };

            _revokeAll($scope.orgId, $scope.member.userId);

            OrgSvcs.save({ organizationId: $scope.orgId, entityType: 'roles' }, grantRolesBean, function() { // Success
              $log.info('Successfully Saved: ' + angular.toJson(grantRolesBean));
              $scope.flipCard($scope.front);
            }, PageLifecycle.handleError);

            _reassignRoles(selectedRoles);
          };

          // Revoke all permissions with warning, this removes the user from the current organization
          $scope.revokeAll = function(size) {
            var options = {
              message: 'This will remove ' + $scope.member.userName + ' from the Organization. Really do this?',
              title: 'Confirm Remove'
            };

            $scope.animationsEnabled = true;

            $scope.toggleAnimation = function () {
              $scope.animationsEnabled = !$scope.animationsEnabled;
            };

            var modalInstance = $uibModal.open({
              animation: $scope.animationsEnabled,
              templateUrl: 'confirmModal.html',
              controller: 'ModalConfirmCtrl',
              size: size,
              resolve: {
                options: function () {
                  return options;
                }
              }
            });

            modalInstance.result.then(function () {
              _revokeAll($scope.orgId, $scope.member.userId);
              $scope.isCardVisible = false;
            }, function () {
              //console.log('Modal dismissed at: ' + new Date());
            });
          };

          // Actual revoke function.
          var _revokeAll = function(orgId, userId) {
            OrgSvcs.delete({ organizationId: orgId, entityType: 'members', entityId: userId },
            function() {
              $log.debug('Successfully revoked all roles for ' + userId);
            }, PageLifecycle.handleError);
          };

          // Now we've modified the roles, we can update to reflect.
          var _reassignRoles = function(newRoles) {
            var matchingRoles = jQuery.grep($scope.roles, function(role:any, _) {
              return jQuery.inArray(role.id, newRoles) >= 0;
            });

            var assignedRoles = matchingRoles.map(function(elem) {
              return {
                roleId : elem.id,
                roleName : elem.name
              }
            });

            $scope.member.roles = assignedRoles;
            $scope.updatedRoles = getRoleIds($scope.member);
          }
        }
      };
    }]);

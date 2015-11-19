/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var OrgMembersController = _module.controller("Apiman.OrgMembersController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope', '$routeParams',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope, $routeParams) => {
            $scope.tab = 'members';
            var params = $routeParams;
            $scope.organizationId = params.org;

            $scope.filterMembers = function(value) {
                if (!value) {
                    $scope.filteredMembers = $scope.members;
                } else {
                    var filtered = [];
                    angular.forEach($scope.members, function(member) {
                        if (member.userName.toLowerCase().indexOf(value.toLowerCase()) > -1 || member.userId.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(member);
                        }
                    });
                    $scope.filteredMembers = filtered;
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
                        $scope.filteredMembers = members;
                        resolve(members);
                    }, reject);
                })
            };
            PageLifecycle.loadPage('OrgMembers', 'orgView', pageData, $scope, function() {
                PageLifecycle.setPageTitle('org-members', [ $scope.org.name ]);
            });
        }])

}

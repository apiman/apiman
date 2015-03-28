/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var OrgMembersController = _module.controller("Apiman.OrgMembersController",
        ['$q', '$scope', '$location', 'OrgSvcs', 'PageLifecycle', '$rootScope',
        ($q, $scope, $location, OrgSvcs, PageLifecycle, $rootScope) => {
            var params = $location.search();
            $scope.organizationId = params.org;

            $scope.filterMembers = function(value) {
                if (!value) {
                    $scope.filteredMembers = $scope.members;
                } else {
                    var filtered = [];
                    for (var i = 0; i < $scope.members.length; i++) {
                        var member = $scope.members[i];
                        if (member.userName.toLowerCase().indexOf(value.toLowerCase()) > -1
                          || member.userId.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                            filtered.push(member);
                        }
                    }
                    $scope.filteredMembers = filtered;
                }
            };

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
                })
            });
            PageLifecycle.loadPage('OrgMembers', promise, $scope);
        }])

}

/// <reference path='../../includes.ts'/>
module ApimanCurrentUser {

    export var _module = angular.module('ApimanCurrentUser', ['ApimanServices']);

    export var CurrentUser = _module.factory('CurrentUser', 
        ['$q', '$rootScope', 'CurrentUserSvcs', 'Logger',
        ($q, $rootScope, CurrentUserSvcs, Logger) => {
            return {
                getCurrentUser: function() {
                    return $rootScope.currentUser;
                },
                getCurrentUserOrgs: function() {
                    var orgs = {};
                    var perms = $rootScope.currentUser.permissions;
                    for (var i = 0; i < perms.length; i++) {
                        var perm = perms[i];
                        orgs[perm.organizationId] = true;
                    }
                    var rval = [];
                    angular.forEach(orgs, function(value, key) {
                      this.push(key);
                    }, rval);
                    return rval;
                },
                hasPermission: function(organizationId, permission) {
                    if (organizationId && $rootScope.permissions) {
                        var permid = organizationId + '||' + permission;
                        return $rootScope.permissions[permid];
                    } else {
                        return false;
                    }
                },
                isMember: function(organizationId) {
                    if (organizationId) {
                        return $rootScope.memberships[organizationId];
                    } else {
                        return false;
                    }
                },
                refresh: function(handler, errorHandler) {
                    $rootScope.currentUser = undefined;
                }
            };
        }]);

}

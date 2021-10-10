import angular = require("angular");

angular.module('ApimanCurrentUser', ['ApimanRPC'])
    .factory('CurrentUser',
    ['$q', '$rootScope', 'CurrentUserSvcs', 'Logger',
    ($q, $rootScope, CurrentUserSvcs, Logger) => {
        return {
            getCurrentUser: function() {
                return $q(function (resolve, reject) {
                    return CurrentUserSvcs.get({what: 'info'}, function (currentUser) {
                        resolve(currentUser);
                    }, reject);
                });
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
                //Logger.debug('Checking for permission {0}||{1} in {2}', organizationId, permission, $rootScope.permissions);
                if ($rootScope.currentUser && $rootScope.currentUser.admin) {
                    return true;
                }
                if (organizationId && $rootScope.permissions) {
                    var permid = organizationId + '||' + permission;
                    return $rootScope.permissions[permid];
                } else {
                    return false;
                }
            },
            isMember: function(organizationId) {
                if (organizationId && $rootScope.memberships) {
                    return $rootScope.memberships[organizationId];
                } else {
                    return false;
                }
            },
            clear: function() {
                $rootScope.currentUser = undefined;
            }
        };
    }]);


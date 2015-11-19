/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var UserActivityController = _module.controller("Apiman.UserActivityController",
        ['$q', '$scope', '$location', 'UserSvcs', 'UserAuditSvcs', 'PageLifecycle', '$routeParams',
        ($q, $scope, $location, UserSvcs, UserAuditSvcs, PageLifecycle, $routeParams) => {
            $scope.tab = 'activity';

            var getNextPage = function(successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                UserAuditSvcs.get({ user: $routeParams.user, page: $scope.currentPage, count: 20 }, function(results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
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
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            };
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('UserActivity', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('user-activity', [ $scope.user.fullName ]);
            });
            
    }])

}

/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var UserActivityController = _module.controller("Apiman.UserActivityController",
        ['$q', '$scope', '$location', 'UserSvcs', 'UserAuditSvcs', 'PageLifecycle', 
        ($q, $scope, $location, UserSvcs, UserAuditSvcs, PageLifecycle) => {
            var params = $location.search();
            $scope.tab = 'activity';

            var getNextPage = function(successHandler, errorHandler) {
                $scope.currentPage = $scope.currentPage + 1;
                UserAuditSvcs.get({ user: params.user, page: $scope.currentPage, count: 20 }, function(results) {
                    var entries = results.beans;
                    successHandler(entries);
                }, errorHandler);
            };

            var promise = $q.all({
                user: $q(function(resolve, reject) {
                    UserSvcs.get({ user: params.user }, function(user) {
                        if (!user.fullName) {
                            user.fullName = user.username;
                        }
                        resolve(user);
                    }, function(error) {
                        reject(error);
                    });
                }),
                auditEntries: $q(function(resolve, reject) {
                    $scope.currentPage = 0;
                    getNextPage(resolve, reject);
                })
            });
            $scope.getNextPage = getNextPage;
            PageLifecycle.loadPage('UserActivity', promise, $scope);
    }])

}

/// <reference path="../apimanPlugin.ts"/>
module Apiman {

    export var ConsumerOrgsController = _module.controller("Apiman.ConsumerOrgsController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger', 'CurrentUser',
        ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger, CurrentUser) => {
            var params = $location.search();
            if (params.q) {
                $scope.orgName = params.q;
            }
            
            $scope.searchOrg = function(value) {
                $location.search('q', value);
            };
            
            var promise = $q.all({
                orgs: $q(function(resolve, reject) {
                    if (params.q) {
                        var body:any = {};
                        body.filters = [];
                        body.filters.push( {"name": "name", "value": "%" + params.q + "%", "operator": "like"});
                        var searchStr = JSON.stringify(body);
                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'organizations' }, searchStr, function(result) { 
                            angular.forEach(result.beans, function(org) {
                                org.isMember = CurrentUser.isMember(org.id);
                            });
                            resolve(result.beans);
                        }, reject);
                    } else {
                        resolve([]);
                    }
                })
            });
            
            PageLifecycle.loadPage('ConsumerOrgs', promise, $scope, function() {
                PageLifecycle.setPageTitle('consumer-orgs');
            });
        }]);

}

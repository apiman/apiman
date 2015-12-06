/// <reference path="../apimanPlugin.ts"/>
/// <reference path="../services.ts"/>
module Apiman {

    export var AdminCatalogController = _module.controller("Apiman.AdminCatalogController",
        ['$q', '$scope', 'ApimanSvcs', 'PageLifecycle', ($q, $scope, ApimanSvcs, PageLifecycle) => {
            $scope.tab = 'catalog';
            
            var body:any = {};
            body.filters = [];
            body.filters.push({ "name" : "name", "value" : "*", "operator" : "like" });
            var searchStr = angular.toJson(body);
            
            var pageData = {
                services: $q(function(resolve, reject) {
                    ApimanSvcs.save({ entityType: 'search', secondaryType: 'serviceCatalogs' }, searchStr, function(reply) {
                        resolve(reply.beans);
                    }, reject);
                })
            };
            
            PageLifecycle.loadPage('AdminCatalog', 'admin', pageData, $scope, function() {
                PageLifecycle.setPageTitle('admin-catalog');
            });
    }])

}

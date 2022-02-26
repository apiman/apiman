import {_module} from "../apimanPlugin";
import angular = require("angular");

_module.controller("Apiman.ConsumerApisController",
        ['$q', '$location', '$scope', 'ApimanSvcs', 'PageLifecycle', 'Logger',
        function ($q, $location, $scope, ApimanSvcs, PageLifecycle, Logger) {
            var params = $location.search();
            if (params.q) {
                $scope.apiName = params.q;
            }

            $scope.searchSvcs = function(value, currentPage, pageSize) {
                $location.search({
                    'q': value,
                    'cp': currentPage,
                    'ps': pageSize
                });
            };

            var pageData = {
                apis: $q(function(resolve, reject) {
                    if (params.q && params.cp && params.ps) {
                        var body:any = {};
                        body.filters = [];

                        body.page = params.cp;
                        body.pageSize = params.ps;

                        body.filters.push( {"name": "name", "value": "*" + params.q + "*", "operator": "like"});
                        var searchStr = angular.toJson(body);

                        ApimanSvcs.save({ entityType: 'search', secondaryType: 'apis' }, searchStr, function(result) {

                            $scope.resultCount = result.totalSize;
                            $scope.currentPage = params.cp;
                            $scope.pageSize = params.ps;

                            resolve(result.beans);
                        }, reject);
                    } else {
                        resolve([]);
                    }
                })
            };


            function loadFirstPage() {
                // only load first page if there are no query params
                if ($scope.apis.length == 0 && !$location.search().q) {
                    $scope.searchSvcs('*', 1, 12);
                }
            }

            PageLifecycle.loadPage('ConsumerApis', undefined, pageData, $scope, function() {
                PageLifecycle.setPageTitle('consumer-apis');
                loadFirstPage();
                $scope.$applyAsync(function() {
                    $('#apiman-search').val('').focus();
                });
            });
        }]);

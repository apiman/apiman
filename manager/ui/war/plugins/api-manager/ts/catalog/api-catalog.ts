import {_module} from "../apimanPlugin";
import angular = require("angular");
import _ = require("lodash");

_module.controller("Apiman.ApiCatalogController",
    [ '$q', 'Logger', '$scope', '$rootScope', '$filter', '$timeout', 'ApiCatalogSvcs', 'PageLifecycle', '$uibModal', 'CurrentUser', 'UserSvcs', '$location',
      function ($q, Logger, $scope, $rootScope, $filter, $timeout, ApiCatalogSvcs, PageLifecycle, $uibModal, CurrentUser, UserSvcs, $location) {

        // set a "rest" as default value for the dropdown
        $scope.epType = "rest";

        var body:any = {};
        body.filters = [];
        body.filters.push({"name": "name", "value": "*", "operator": "like"});

        var namespace = $location.hash();

        if (namespace) {
          body.filters.push({"name": "namespace", "value": namespace, "operator": "eq"});
        }

        var searchStr = angular.toJson(body);

        $scope.reverse = false;

        $scope.tags = [];
        $scope.selected = {};
        $scope.selected.tags = [];

        $scope.filterApis = function (searchText) {
          $scope.criteria = {
            name: searchText
          };
        };

        $scope.clear = function() {
          $scope.selected.tags = [];
        };

        $scope.addTag = function(tag) {
          if(_.find($scope.selected.tags, tag)) {
            console.log('This tag already existed on here, not adding again...');
            return;
          } else {
            $scope.selected.tags.push(tag);
          }
        };

        $scope.removeTag = function(tag) {
          if(_.find($scope.selected.tags, tag)) {
            _.pull($scope.selected.tags, tag);
          } else {
            return console.log('This tag doesn\'t exist on here.');
          }
        };

        $scope.hideInternal = true;

        $scope.isInternal = function (actual, expected) {
          if (!expected) {
            return true;
          }
          if (!actual) {
            return false;
          }
          if (!actual.id && !actual.name) {
            return false;
          }
          if (actual.internal == true) {
            return false;
          }
          return true;
        };

        $scope.apiEndpoint = function (api) {
          if (api.routeEndpoint) {
            return api.routeEndpoint;
          }
          return api.endpoint;
        };

        $scope.selectNamespace = function (ns) {
          $location.hash(ns.name);
        };

        $scope.importApi = function (api) {
          var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'importApiModal.html',
            controller: 'Apiman.ImportApiController',
            resolve: {
              api: function () {
                var copyOf = angular.copy(api);
                copyOf.initialVersion = '1.0';
                return copyOf;
              },
              orgs: function () {
                return $scope.orgs;
              }
            }
          });
        };
        $scope.tagLabel = function(tag) {
          var idx = tag.indexOf("=");
          if (idx == -1) {
            return tag;
          } else {
            return tag.slice(idx + 1);
          }
        };
        $scope.tagTitle = function(tag) {
          return "Filter by tag: " + tag;
        };

        var pageData = {
          namespaces: $q(function (resolve, reject) {
            ApiCatalogSvcs.getNamespaces(function (reply) {
              angular.forEach(reply, function (ns) {
                if ((namespace && ns.name == namespace) || (!namespace && ns.current)) {
                  $scope.namespace = ns;
                }
              });
              resolve(reply);
            }, reject);
          }),
          apis: $q(function (resolve, reject) {
            $scope.hasInternalApis = false;
            ApiCatalogSvcs.search(searchStr, function (reply) {
              angular.forEach(reply.beans, function (entry) {
                if (entry.internal) {
                  $scope.hasInternalApis = true;
                }
              });
              resolve(reply.beans);
            }, reject);
          }),
          orgs: $q(function (resolve, reject) {
            return CurrentUser.getCurrentUser().then(function (currentUser) {
              return UserSvcs.query({ user: currentUser.username, entityType: 'apiorgs' }, resolve, reject);
            })
          })
        };


        let apiAdjustments = function (api) {
          api.iconIsUrl = false;
          if (!api.icon) {
            api.icon = 'puzzle-piece';
          }
          if (api.icon.indexOf('http') == 0) {
            api.iconIsUrl = true;
          }
          api.ticon = 'fa-file-text-o';
          if (api.endpointType == 'soap' || api.endpointType == 'ui') {
            api.ticon = 'fa-file-code-o';
          }
          if (api.routeDefinitionUrl != null) {
            api.definitionUrl = api.routeDefinitionUrl;
          }
        };

        $scope.userReloadAPICatalog = function(){
          $rootScope.pageState = 'loading';
          $scope.apis = null;

          let resyncBody:any = {};
          resyncBody.filters = [];

          // Fix string that will evaluated by the e2e-catalog-plugin
          resyncBody.filters.push({"name": "name", "value": "__resyncAPICatalogByUser__", "operator": "like"});
          resyncBody.filters.push({"name": "namespace", "value": "__resyncAPICatalogByUser__", "operator": "eq"});

          let resyncSearchString = angular.toJson(resyncBody);

          let target = $q(function (resolve, reject) {
            $scope.hasInternalApis = false;
            ApiCatalogSvcs.search(resyncSearchString, function (reply) {
              angular.forEach(reply.beans, function (entry) {
                if (entry.internal) {
                  $scope.hasInternalApis = true;
                }
              });
              resolve(reply.beans);
            }, reject);
          });

          target.then(data => {
            $scope.apis = data;
            angular.forEach($scope.apis, function (api) {
              apiAdjustments(api)
            });
            $scope.tags = _.uniq(_.flatten(_.map($scope.apis, 'tags')));
            $rootScope.pageState = 'loaded';
          })
        };


        PageLifecycle.loadPage('ApiCatalog', undefined, pageData, $scope, function () {
          angular.forEach($scope.apis, function (api) {
            apiAdjustments(api)
          });
          $scope.tags = _.uniq(_.flatten(_.map($scope.apis, 'tags')));
          PageLifecycle.setPageTitle('api-catalog');
        });
      }
    ]);

_module.controller("Apiman.ImportApiController",
    [ '$q', '$rootScope', 'Logger', '$scope', 'OrgSvcs', 'PageLifecycle', '$uibModalInstance', 'api', 'orgs',
      function ($q, $rootScope, Logger, $scope, OrgSvcs, PageLifecycle, $uibModalInstance, api, orgs) {

        var recentOrg = $rootScope.mruOrg;
        $scope.api = api;
        $scope.orgs = orgs;

        if (recentOrg) {
          $scope.selectedOrg = recentOrg;
        } else if (orgs.length > 0) {
          $scope.selectedOrg = orgs[0];
        }

        $scope.setOrg = function (org) {
          $scope.selectedOrg = org;
        };

        $scope.import = function () {
          $scope.importButton.state = 'in-progress';
          var newApi = {
            'name': $scope.api.name,
            'description': $scope.api.description,
            'initialVersion': $scope.api.initialVersion,
            'endpoint': $scope.api.endpoint,
            'endpointType': ($scope.api.endpointType === 'ui') ? 'rest' : $scope.api.endpointType,
            'definitionUrl': $scope.api.definitionUrl,
            'definitionType': $scope.api.definitionType

          };
          OrgSvcs.save({organizationId: $scope.selectedOrg.id, entityType: 'apis'}, newApi, function (reply) {
            $uibModalInstance.dismiss('cancel');
            PageLifecycle.redirectTo('/orgs/{0}/apis/{1}/{2}', reply.organization.id, reply.id, $scope.api.initialVersion);
          }, function (error) {
            $uibModalInstance.dismiss('cancel');
            PageLifecycle.handleError(error);
          });
        };

        $scope.cancel = function () {
          $uibModalInstance.dismiss('cancel');
        };
      }
    ]);

_module.controller("Apiman.ApiCatalogDefController",
    [ '$q', '$scope', 'ApiCatalogSvcs', 'PageLifecycle', '$routeParams', '$window', 'Logger', 'ApiDefinitionSvcs', 'Configuration',
      function ($q, $scope, ApiCatalogSvcs, PageLifecycle, $routeParams, $window, Logger, ApiDefinitionSvcs, Configuration) {
        $scope.params = $routeParams;
        $scope.chains = {};

        var name = $scope.params.name;

        var body:any = {};

        body.filters = [];
        body.filters.push({"name": "name", "value": $scope.params.name, "operator": "like"});

        var searchStr = angular.toJson(body);

        var pageData = {
          apis: $q(function (resolve, reject) {
            ApiCatalogSvcs.search(searchStr, function (reply) {
              resolve(reply.beans);
              $scope.api = reply.beans[0];
              if ($scope.api.routeDefinitionUrl != null) {
                $scope.api.definitionUrl = $scope.api.routeDefinitionUrl;
              }
            }, reject);
          })
        };

        PageLifecycle.loadPage('ApiCatalogDef', undefined, pageData, $scope, function () {

          $scope.hasError = false;

          PageLifecycle.setPageTitle('api-catalog-def', [$scope.params.name]);
        });
      }
    ]);
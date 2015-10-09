/* global _ */
/* global angular */
/* global jQuery */

/*globals window document Logger CustomEvent URI _ $ angular hawtioPluginLoader jQuery*/

// Polyfill custom event if necessary since we kinda need it
(function () {
  if (!window.CustomEvent) {
    function CustomEvent ( event, params ) {
      params = params || { bubbles: false, cancelable: false, detail: undefined };
      var evt = document.createEvent( 'CustomEvent' );
      evt.initCustomEvent( event, params.bubbles, params.cancelable, params.detail );
      return evt;
    }

    CustomEvent.prototype = window.Event.prototype;
    window.CustomEvent = CustomEvent;
  }
})();


var HawtioMainNav;
(function(HawtioMainNav) {

  function documentBase($document) {
    var base = $document.find('base');
    return base.attr('href');
  }

  function trimLeading(text, prefix) {
    if (text && prefix) {
      if (_.startsWith(text, prefix) || text.indexOf(prefix) === 0) {
        return text.substring(prefix.length);
      }
    }
    return text;
  }

  HawtioMainNav.pluginName = 'hawtio-nav';
  var log = Logger.get(HawtioMainNav.pluginName);

  // Actions class with some pre-defined actions 
  var Actions = (function() {
    function Actions() {}
    Object.defineProperty(Actions, "ADD", {
      get: function() {
        return 'hawtio-main-nav-add';
      },
      enumerable: true,
      configurable: true
    });
    Object.defineProperty(Actions, "REMOVE", {
      get: function() {
        return 'hawtio-main-nav-remove';
      },
      enumerable: true,
      configurable: true
    });
    Object.defineProperty(Actions, "CHANGED", {
      get: function() {
        return 'hawtio-main-nav-change';
      },
      enumerable: true,
      configurable: true
    });
    Object.defineProperty(Actions, "REDRAW", {
      get: function() {
        return 'hawtio-main-nav-redraw';
      },
      enumerable: true,
      configurable: true
    });
    return Actions;
  })();
  HawtioMainNav.Actions = Actions;

  // Class RegistryImpl
  var RegistryImpl = (function() {
    function RegistryImpl(root) {
      this.items = [];
      this.root = root;
      /*
         this.on(HawtioMainNav.Actions.ADD, 'log', function (item) {
         console.log('Adding item with id: ', item.id);
         });
         this.on(HawtioMainNav.Actions.REMOVE, 'log', function (item) {
         console.log('Removing item with id: ', item.id);
         });
         */
    }
    RegistryImpl.prototype.builder = function() {
      return new HawtioMainNav.NavItemBuilderImpl();
    };
    RegistryImpl.prototype.add = function(item) {
      var _this = this;
      var items = [];
      for (var _i = 1; _i < arguments.length; _i++) {
        items[_i - 1] = arguments[_i];
      }
      var toAdd = _.union([item], items);
      this.items = _.union(this.items, toAdd);
      toAdd.forEach(function(item) {
        _this.root.dispatchEvent(new CustomEvent(HawtioMainNav.Actions.ADD, {
          detail: {
            item: item
          }
        }));
      });
      this.root.dispatchEvent(new CustomEvent(HawtioMainNav.Actions.CHANGED, {
        detail: {
          items: this.items
        }
      }));
      this.root.dispatchEvent(new CustomEvent(HawtioMainNav.Actions.REDRAW, {
        detail: {}
      }));
    };
    RegistryImpl.prototype.remove = function(search) {
      var _this = this;
      var removed = _.remove(this.items, search);
      removed.forEach(function(item) {
        _this.root.dispatchEvent(new CustomEvent(HawtioMainNav.Actions.REMOVE, {
          detail: {
            item: item
          }
        }));
      });
      this.root.dispatchEvent(new CustomEvent(HawtioMainNav.Actions.CHANGED, {
        detail: {
          items: this.items
        }
      }));
      this.root.dispatchEvent(new CustomEvent(HawtioMainNav.Actions.REDRAW, {
        detail: {}
      }));
      return removed;
    };
    RegistryImpl.prototype.iterate = function(iterator) {
      this.items.forEach(iterator);
    };
    RegistryImpl.prototype.selected = function() {
      var valid = _.filter(this.items, function(item) {
        if (!item.isValid) {
          return true;
        }
        return item.isValid()
      });
      var answer = _.find(valid, function(item) {
        if (!item.isSelected) {
          return false;
        }
        return item.isSelected();
      });
      return answer;
    };
    RegistryImpl.prototype.on = function(action, key, fn) {
      var _this = this;
      switch (action) {
        case HawtioMainNav.Actions.ADD:
          this.root.addEventListener(HawtioMainNav.Actions.ADD, function(event) {
            //log.debug("event key: ", key, " event: ", event);
            fn(event.detail.item);
          });
          if (this.items.length > 0) {
            this.items.forEach(function(item) {
              _this.root.dispatchEvent(new CustomEvent(HawtioMainNav.Actions.ADD, {
                detail: {
                  item: item
                }
              }));
            });
          }
          break;
        case HawtioMainNav.Actions.REMOVE:
          this.root.addEventListener(HawtioMainNav.Actions.REMOVE, function(event) {
            //log.debug("event key: ", key, " event: ", event);
            fn(event.detail.item);
          });
          break;
        case HawtioMainNav.Actions.CHANGED:
          this.root.addEventListener(HawtioMainNav.Actions.CHANGED, function(event) {
            //log.debug("event key: ", key, " event: ", event);
            fn(event.detail.items);
          });
          if (this.items.length > 0) {
            this.root.dispatchEvent(new CustomEvent(HawtioMainNav.Actions.CHANGED, {
              detail: {
                items: _this.items
              }
            }));
          }
          break;
        case HawtioMainNav.Actions.REDRAW:
          this.root.addEventListener(HawtioMainNav.Actions.REDRAW, function(event) {
            //log.debug("event key: ", key, " event: ", event);
            fn(event);
          });
          var event = new CustomEvent(HawtioMainNav.Actions.REDRAW, {
            detail: {
              text: ''
            }
          });
          this.root.dispatchEvent(event);
          break;
        default:
      }
    };
    return RegistryImpl;
  })();

  // Factory for registry, used to create angular service
  function createRegistry(root) {
    return new RegistryImpl(root);
  }
  HawtioMainNav.createRegistry = createRegistry;

  // Class NavItemBuilderImpl
  var NavItemBuilderImpl = (function() {
    function NavItemBuilderImpl() {
      this.self = {
        id: ''
      };
    }
    NavItemBuilderImpl.join = function() {
      var paths = [];
      for (var _i = 0; _i < arguments.length; _i++) {
        paths[_i - 0] = arguments[_i];
      }
      var tmp = [];
      var length = paths.length - 1;
      paths.forEach(function (path, index) {
        if (!path || path === '') {
          return;
        }
        if (index !== 0 && path.charAt(0) === '/') {
          path = path.slice(1);
        }
        if (index !== length && path.charAt(path.length) === '/') {
          path = path.slice(0, path.length - 1);
        }
        if (path && path !== '') {
          tmp.push(path);
        }
      });
      var rc = tmp.join('/');
      return rc;
    };
    NavItemBuilderImpl.prototype.id = function(id) {
      this.self.id = id;
      return this;
    };
    NavItemBuilderImpl.prototype.rank = function(rank) {
      this.self.rank = rank;
      return this;
    };
    NavItemBuilderImpl.prototype.title = function(title) {
      this.self.title = title;
      return this;
    };
    NavItemBuilderImpl.prototype.tooltip = function(tooltip) {
      this.self.tooltip = tooltip;
      return this;
    };
    NavItemBuilderImpl.prototype.page = function(page) {
      this.self.page = page;
      return this;
    };
    NavItemBuilderImpl.prototype.reload = function(reload) {
      this.self.reload = reload;
      return this;
    };
    NavItemBuilderImpl.prototype.attributes = function(attributes) {
      this.self.attributes = attributes;
      return this;
    };
    NavItemBuilderImpl.prototype.linkAttributes = function(attributes) {
      this.self.linkAttributes = attributes;
      return this;
    };
    NavItemBuilderImpl.prototype.context = function(context) {
      this.self.context = context;
      return this;
    };
    NavItemBuilderImpl.prototype.href = function(href) {
      this.self.href = href;
      return this;
    };
    NavItemBuilderImpl.prototype.click = function(click) {
      this.self.click = click;
      return this;
    };
    NavItemBuilderImpl.prototype.isSelected = function(isSelected) {
      this.self.isSelected = isSelected;
      return this;
    };
    NavItemBuilderImpl.prototype.isValid = function(isValid) {
      this.self.isValid = isValid;
      return this;
    };
    NavItemBuilderImpl.prototype.show = function(show) {
      this.self.show = show;
      return this;
    };
    NavItemBuilderImpl.prototype.template = function(template) {
      this.self.template = template;
      return this;
    };
    NavItemBuilderImpl.prototype.defaultPage = function(defaultPage) {
      this.self.defaultPage = defaultPage;
      return this;
    };
    NavItemBuilderImpl.prototype.tabs = function(item) {
      var items = [];
      for (var _i = 1; _i < arguments.length; _i++) {
        items[_i - 1] = arguments[_i];
      }
      this.self.tabs = _.union(this.self.tabs, [item], items);
      return this;
    };
    NavItemBuilderImpl.prototype.subPath = function(title, path, page, rank, reload, isValid) {

      var parent = this.self;
      if (!this.self.tabs) {
        this.self.tabs = [];
      }
      var tab = {
        id: parent.id + '-' + path,
        title: function() {
          return title;
        },
        href: function() {
          if (parent.href) {
            return NavItemBuilderImpl.join(parent.href(), path);
          }
          return path;
        }
      };
      if (!_.isUndefined(page)) {
        tab.page = function() {
          return page;
        };
      }
      if (!_.isUndefined(rank)) {
        tab.rank = rank;
      }
      if (!_.isUndefined(reload)) {
        tab.reload = reload;
      }
      if (!_.isUndefined(isValid)) {
        tab.isValid = isValid;
      }
      this.self.tabs.push(tab);
      return this;
    };
    NavItemBuilderImpl.prototype.build = function() {
      var answer = _.cloneDeep(this.self);
      this.self = {
        id: ''
      };
      return answer;
    };
    return NavItemBuilderImpl;
  })();
  HawtioMainNav.NavItemBuilderImpl = NavItemBuilderImpl;

  // Factory functions
  HawtioMainNav.createBuilder = function() {
    return new HawtioMainNav.NavItemBuilderImpl();
  };

  // Plugin initialization
  var _module = angular.module(HawtioMainNav.pluginName, ['ngRoute']);
  HawtioMainNav._module = _module;

  _module.constant('layoutFull', 'templates/main-nav/layoutFull.html');

  _module.config(['$locationProvider', '$routeProvider', function($locationProvider, $routeProvider) {
    $locationProvider.html5Mode({
      enabled: true,
      requireBase: true
    });
    $routeProvider.otherwise({ templateUrl: 'templates/main-nav/welcome.html' });
  }]);

  _module.controller('HawtioNav.WelcomeController', ['$scope', '$location', 'WelcomePageRegistry', 'HawtioNav', '$timeout', '$document', function($scope, $location, welcome, nav, $timeout, $document) {

    function gotoNavItem(item) {
      if (item && item.href) {
        var href = trimLeading(item.href(), documentBase($document));
        var uri = new URI(href);
        var search = _.merge($location.search(), uri.query(true));
        log.debug("Going to item id: ", item.id, " href: ", uri.path(), " query: ", search);
        $timeout(function() {
          $location.path(uri.path()).search(search);
        }, 10);
      }
    }

    function gotoFirstAvailableNav() {
      var candidates = [];
      nav.iterate(function(item) {
        var isValid = item.isValid || function() { return true; };
        var show = item.show || function() { return true; };
        if (isValid() && show()) {
          candidates.push(item);
        }
      });
      var rankedCandidates = sortByRank(candidates);
      gotoNavItem(rankedCandidates[0]);
    }

    $timeout(function() {
      var search = $location.search();
      if (search.tab) {
        var tab = search.tab;
        var selected;
        nav.iterate(function (item) {
          if (!selected && item.id === tab) {
            selected = item;
          }
        });
        if (selected) {
          gotoNavItem(selected);
          return;
        }
      }
      var candidates = [];
      nav.iterate(function(item) {
        if ('defaultPage' in item) {
          var page = item.defaultPage;
          if (!('rank' in page)) {
            candidates.push(item);
            return;
          }
          var index = _.findIndex(candidates, function(i) {
            if ('rank' in i && item.rank > i.rank) {
              return true;
            }
          });
          if (index < 0) {
            candidates.push(item);
          } else {
            candidates.splice(index, 0, item);
          }
        }
      });

      function welcomePageFallback() {
        if (welcome.pages.length === 0) {
          log.debug("No welcome pages, going to first available nav");
          gotoFirstAvailableNav();
        }
        var sortedPages = _.sortBy(welcome.pages, function(page) { return page.rank; });
        var page = _.find(sortedPages, function(page) {
          if ('isValid' in page) {
            return page.isValid();
          }
          return true;
        });
        if (page) {
          gotoNavItem(page);
        } else {
          gotoFirstAvailableNav();
        }
      }

      function evalCandidates(candidates) {
        if (candidates.length === 0) {
          welcomePageFallback();
          return;
        }
        var item = candidates.pop();
        var remaining = candidates;
        log.debug("Trying candidate: ", item, " remaining: ", remaining);
        if (!item) {
          welcomePageFallback();
          return;
        }
        var func = item.defaultPage.isValid;
        if (func) {
          var yes = function() {
            gotoNavItem(item);
          };
          var no = function() {
            evalCandidates(remaining);
          };
          try {
            func(yes, no);
          } catch (err) {
            log.debug("Failed to eval item: ", item.id, " error: ", err);
            no();
          }
        } else {
          evalCandidates(remaining);
        }
      }
      evalCandidates(candidates);
    }, 500);
  }]);

  _module.controller('HawtioNav.ViewController', ['$scope', '$route', '$location', 'layoutFull', 'viewRegistry', function($scope, $route, $location, layoutFull, viewRegistry) {

    findViewPartial();

    $scope.$on("$routeChangeSuccess", function (event, current, previous) {
      findViewPartial();
    });

    function searchRegistryViaQuery(query) {
      var answer = undefined;
      if (!query || _.keys(query).length === 0) {
        log.debug("No query, skipping query matching");
        return;
      }
      var keys = _.keys(viewRegistry);
      var candidates = _.filter(keys, function(key) { return key.charAt(0) === '{'; });
      candidates.forEach(function(candidate) {
        if (!answer) {
          try {
            var obj = angular.fromJson(candidate);
            if (_.isObject(obj)) {
              _.merge(obj, query, function(a, b) {
                if (a) {
                  if (a === b) {
                    answer = viewRegistry[candidate];
                  } else {
                    answer = undefined;
                  }
                }
              });
            }
          } catch (e) {
            // ignore and move on...
            log.debug("Unable to parse json: ", candidate);
          }
        }
      });
      return answer;
    }

    function searchRegistry(path) {
      var answer = undefined;
      _.forIn(viewRegistry, function (value, key) {
        if (!answer) {
          try {
            var reg = new RegExp(key, "");
            if (reg.exec(path)) {
              answer = value;
            }
          } catch (e) {
            log.debug("Invalid RegExp " + key + " for viewRegistry value: " + value);
          }
        }
      });
      return answer;
    }

    function findViewPartial() {
      var answer = null;
      var hash = $location.search();
      answer = searchRegistryViaQuery(hash);
      if (answer) {
        log.debug("View partial matched on query");
      }
      if (!answer) {
        var path = $location.path();
        if (path) {
          answer = searchRegistry(path);
          if (answer) {
            log.debug("View partial matched on path name");
          }
        }
      }
      if (!answer) {
        answer = layoutFull;
        log.debug("Using default view partial");
      }
      $scope.viewPartial = answer;

      log.debug("Using view partial: " + answer);
      return answer;
    }
  }]);

  _module.run(['HawtioNav', '$rootScope', '$route', '$document', function(HawtioNav, $rootScope, $route, $document) {
    HawtioNav.on(HawtioMainNav.Actions.CHANGED, "$apply", function(item) {
      if(!$rootScope.$$phase) {
        $rootScope.$apply();
      }
    });

    var href = documentBase($document);

    function applyBaseHref(item) {
      if (!item.preBase) {
        item.preBase = item.href;
        item.href = function() {
          if (href) {
            var preBase = item.preBase();
            if (preBase && preBase.charAt(0) === '/') {
              preBase = preBase.substr(1);
	            return href + preBase;
            }
          }
          return item.preBase();
        };
      }
    }
    HawtioNav.on(HawtioMainNav.Actions.ADD, "htmlBaseRewriter", function(item) {
			if (item.href) {
	      applyBaseHref(item);
	      _.forEach(item.tabs, applyBaseHref);
			}
    });
    HawtioNav.on(HawtioMainNav.Actions.ADD, "$apply", function(item) {
      var oldClick = item.click;
      item.click = function($event) {
        if (!($event instanceof jQuery.Event)) {
          try {
            $rootScope.$apply();
          } catch (e) {
            // ignore
          }
        }
        if (oldClick) {
          oldClick($event);
        }
      };
    });
    $route.reload();
    log.debug("loaded");
  }]);
  hawtioPluginLoader.addModule(HawtioMainNav.pluginName);
  hawtioPluginLoader.addModule("ngRoute");

  // helper function for testing nav items
  function itemIsValid(item) {
    if (!('isValid' in item)) {
      return true;
    }
    if (_.isFunction(item.isValid)) {
      return item.isValid();
    }
    return false;
  }

  // Construct once and share between invocations to avoid memory leaks
  var tmpLink = $('<a>');
  function addIsSelected($location, item) {
    if (!('isSelected' in item) && 'href' in item) {
      item.isSelected = function() {
        // item.href() might be relative, in which
        // case we should let the browser resolve
        // what the full path should be
        tmpLink.attr("href", item.href());
        var href = new URI(tmpLink[0].href);
        var itemPath = trimLeading(href.path(), '/');
        if (itemPath === '') {
          log.debug("nav item: ", item.id, " returning empty href, can't be selected");
          return false;
        }
        var current = new URI();
        var path = trimLeading(current.path(), '/');
        var query = current.query(true);
        var mainTab = query['main-tab'];
        var subTab = query['sub-tab'];
        if (itemPath !== '' && !mainTab && !subTab) {
          if (item.isSubTab && _.startsWith(path, itemPath)) {
            return true;
          }
          if (item.tabs) {
            var answer = _.any(item.tabs, function(subTab) {
              var answer = subTab.isSelected();
              return answer;
            });
            if (answer) {
              return true;
            }
          }
        }
        var answer = false;
        if (item.isSubTab) {
          if (!subTab) {
            answer = _.startsWith(path, itemPath);
          } else {
            answer = subTab === item.id;
          }
        } else {
          if (!mainTab) {
            answer = _.startsWith(path, itemPath);
          } else {
            answer = mainTab === item.id;
          }
        }
        return answer;
      };
    }
  }

  function drawNavItem($templateCache, $compile, scope, element, item) {
    if (!itemIsValid(item)) {
      return;
    }
    var newScope = scope.$new();
    item.hide = function() { return item.show && !item.show(); };
    newScope.item = item;
    var template = null;
    if (_.isFunction(item.template)) {
      template = item.template();
    } else {
      template = $templateCache.get('templates/main-nav/navItem.html');
    }
    if (item.attributes || item.linkAttributes) {
      var tmpEl = $(template);
      if (item.attributes) {
        tmpEl.attr(item.attributes);
      }
      if (item.linkAttributes) {
        tmpEl.find('a').attr(item.linkAttributes);
      }
      template = tmpEl.prop('outerHTML');
    }
    element.append($compile(template)(newScope));
  }

  function sortByRank(collection) {
    var answer = [];
    collection.forEach(function(item) {
      rankItem(item, answer);
    });
    return answer;
  }

  function rankItem(item, collection) {
    if (!('rank' in item) || collection.length === 0) {
      collection.push(item);
      return;
    }
    var index = _.findIndex(collection, function(i) {
      if ('rank' in i && item.rank > i.rank) {
        return true;
      }
    });
    if (!('rank' in collection[0])) {
      index = 0;
    }
    if (index < 0) {
      collection.push(item);
    } else {
      collection.splice(index, 0, item);
    }
  }

  HawtioMainNav._module.directive('hawtioSubTabs', ['HawtioNav', '$templateCache', '$compile', '$location', '$rootScope', function(HawtioNav, $templateCache, $compile, $location, $rootScope) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {

        scope.$watch(_.debounce(function() {
          var selected = HawtioNav.selected();
          if (scope.selected !== selected) {
            scope.selected = selected;
            scope.$broadcast('hawtio-nav-subtab-redraw');
            scope.$apply();
          }
        }, 100, { trailing: true }));

        scope.$on('hawtio-nav-subtab-redraw', function() {
          log.debug("Redrawing sub-tabs");
          element.empty();
          var selectedNav = scope.selected
          if (!selectedNav || !selectedNav.tabs) {
            return;
          }
          if (attrs['showHeading']) {
            var heading = angular.extend({}, selectedNav, {
              template: function() { return $templateCache.get('templates/main-nav/subTabHeader.html'); }});
              drawNavItem($templateCache, $compile, scope, element, heading);
          }
          var rankedTabs = sortByRank(selectedNav.tabs);
          rankedTabs.forEach(function(item) {
            drawNavItem($templateCache, $compile, scope, element, item);
          });
        });
        scope.$broadcast('hawtio-nav-subtab-redraw');
      }
    };
  }]);

  HawtioMainNav._module.directive("hawtioMainNav", ["HawtioNav", "$templateCache", "$compile", "$location", "$rootScope", function(HawtioNav, $templateCache, $compile, $location, $rootScope) {
    var config = {
      nav: {},
      numKeys: 0,
      numValid: 0
    };
    var iterationFunc = function(item) {
      if (itemIsValid(item)) {
        config.numValid = config.numValid + 1;
      }
    };
    HawtioNav.on(HawtioMainNav.Actions.ADD, 'subTabEnricher', function(item) {
      if (item.tabs && item.tabs.length > 0) {
        item.tabs.forEach(function (subItem) {
          subItem.isSubTab = true;
          if (!subItem.oldHref) {
            subItem.oldHref = subItem.href;
            subItem.href = function() {
              var uri = new URI(subItem.oldHref());
              if (uri.path() === "") {
                return "";
              }
              uri.search(function(search) {
                _.merge(search, uri.query(true));
                if (!search['main-tab']) {
                  search['main-tab'] = item.id;
                }
                search['sub-tab'] = subItem.id;
              });
              return uri.toString();
            };
          }
        });
      }
    });
    HawtioNav.on(HawtioMainNav.Actions.ADD, 'hrefEnricher', function(item) {
      item.isSubTab = false;
      if (item.href && !item.oldHref) {
        item.oldHref = item.href;
        item.href = function() {
          var oldHref = item.oldHref();
          if (!oldHref) {
            log.debug("Item: ", item.id, " returning null for href()");
            return "";
          }
          var uri = new URI(oldHref);
          if (uri.path() === "") {
            return "";
          }
          uri.search(function(search) {
            if (!search['main-tab']) {
              search['main-tab'] = item.id;
            }
            _.merge(search, uri.query(true));
            if (!search['sub-tab'] && item.tabs && item.tabs.length > 0) {
              var sorted = sortByRank(item.tabs);
              search['sub-tab'] = sorted[0].id;
            }
          });
          return uri.toString();
        };
      }
    });
    HawtioNav.on(HawtioMainNav.Actions.ADD, 'isSelectedEnricher', function(item) {
      addIsSelected($location, item);
      if (item.tabs) {
        item.tabs.forEach(function(item) { addIsSelected($location, item); });
      }
    });
    HawtioNav.on(HawtioMainNav.Actions.ADD, 'PrimaryController', function(item) {
      config.nav[item.id] = item;
    });
    HawtioNav.on(HawtioMainNav.Actions.REMOVE, 'PrimaryController', function(item) {
      delete config.nav[item.id];
    });
    HawtioNav.on(HawtioMainNav.Actions.CHANGED, 'PrimaryController', function(items) {
      config.numKeys = items.length;
      config.numValid = 0;
      items.forEach(iterationFunc);
    });
    return {
      restrict: 'A',
      replace: false,
      controller: ["$scope", "$element", "$attrs", function($scope, $element, $attrs) {
        $scope.config = config;
        $scope.$on('hawtio-nav-redraw', function() {
          log.debug("Redrawing main nav");
          $element.empty();

          var rankedContexts = [];
          // first add any contextual menus (like perspectives)
          HawtioNav.iterate(function(item) {
            if (!('context' in item)) {
              return;
            }
            if (!item.context) {
              return;
            }
            rankItem(item, rankedContexts);
          });
          rankedContexts.forEach(function (item) {
            drawNavItem($templateCache, $compile, $scope, $element, item);
          });
          // then add the rest of the nav items
          var rankedTabs = [];
          HawtioNav.iterate(function(item) {
            if (item.context) {
              return;
            }
            rankItem(item, rankedTabs);
          });
          rankedTabs.forEach(function (item) {
            drawNavItem($templateCache, $compile, $scope, $element, item);
          });
        });
      }],
      link: function(scope, element, attr) {
        scope.$watch(_.debounce(function() {
          var oldValid = config.numValid;
          var oldKeys = config.numKeys;
          config.numValid = 0;
          config.numKeys = 0;
          HawtioNav.iterate(iterationFunc);
          if (config.numValid !== oldValid || config.numKeys !== oldKeys) {
            scope.$broadcast('hawtio-nav-redraw');
            scope.$apply();
          }
        }, 500, { trailing: true }));
        scope.$broadcast('hawtio-nav-redraw');
      }
    };
  }]);

  // provider so it's possible to get a nav builder in _module.config()
  HawtioMainNav._module.provider('HawtioNavBuilder', [function HawtioNavBuilderProvider() {
    this.$get = function() {
      return {};
    };
    this.create = function() {
      return HawtioMainNav.createBuilder();
    };
    this.join = NavItemBuilderImpl.join;

    function setRoute($routeProvider, tab) {
      log.debug("Setting route: ", tab.href(), " to template URL: ", tab.page());
      var config = {
        templateUrl: tab.page()
      };
      if (!_.isUndefined(tab.reload)) {
        config.reloadOnSearch = tab.reload;
      }
      $routeProvider.when(tab.href(), config);
    }
    this.configureRouting = function($routeProvider, tab) {
      if (_.isUndefined(tab.page)) {
        if (tab.tabs) {
          var target = _.first(tab.tabs)['href'];
          if (target) {
            log.debug("Setting route: ", tab.href(), " to redirect to ", target());
            $routeProvider.when(tab.href(), {
              reloadOnSearch: tab.reload,
              redirectTo: target()
            });
          }
        }
      } else {
        setRoute($routeProvider, tab);
      }
      if (tab.tabs) {
        tab.tabs.forEach(function(tab) {
          return setRoute($routeProvider, tab);
        });
      }
    };
  }]);

  HawtioMainNav._module.factory('HawtioPerspective', [function() {
    var log = Logger.get('hawtio-dummy-perspective');
    return {
      add: function(id, perspective) {
        log.debug("add called for id: ", id);
      },
      remove: function(id) {
        log.debug("remove called for id: ", id);
        return undefined;
      },
      setCurrent: function(id) {
        log.debug("setCurrent called for id: ", id);
      },
      getCurrent: function(id) {
        log.debug("getCurrent called for id: ", id);
        return undefined;
      },
      getLabels: function() {
        return [];
      }
    };
  }]);

  HawtioMainNav._module.factory('WelcomePageRegistry', [function() {
    return {
      pages: []
    };
  }]);

  HawtioMainNav._module.factory('HawtioNav', ['$window', '$rootScope', function($window, $rootScope) {
    var registry = HawtioMainNav.createRegistry(window);
    return registry;
  }]);

})(HawtioMainNav || (HawtioMainNav = {}));



angular.module("hawtio-nav").run(["$templateCache", function($templateCache) {$templateCache.put("templates/main-nav/layoutFull.html","<div ng-view></div>\n\n\n");
$templateCache.put("templates/main-nav/layoutTest.html","<div>\n  <h1>Test Layout</h1>\n  <div ng-view>\n\n\n  </div>\n</div>\n\n\n");
$templateCache.put("templates/main-nav/navItem.html","<li ng-class=\"{ active: item.isSelected() }\" ng-hide=\"item.hide()\">\n  <a ng-href=\"{{item.href()}}\" ng-click=\"item.click($event)\" ng-bind-html=\"item.title()\" title=\"{{item.tooltip()}}\"></a>\n</li>\n");
$templateCache.put("templates/main-nav/subTabHeader.html","<li class=\"header\">\n  <a href=\"\"><strong>{{item.title()}}</strong></a>\n</li>\n");
$templateCache.put("templates/main-nav/welcome.html","<div ng-controller=\"HawtioNav.WelcomeController\"></div>\n");}]);
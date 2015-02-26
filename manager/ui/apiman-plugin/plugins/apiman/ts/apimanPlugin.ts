/// <reference path="../../includes.ts"/>
/// <reference path="apimanGlobals.ts"/>
/// <reference path="services.ts"/>
module Apiman {

  export var _module = angular.module(Apiman.pluginName, ['actionServices','userServices','organizationServices']);

  var tab = undefined;

  _module.config(['$locationProvider', '$routeProvider', 'HawtioNavBuilderProvider', ($locationProvider, $routeProvider:ng.route.IRouteProvider, builder:HawtioMainNav.BuilderFactory) => {
    tab = builder.create()
      .id(Apiman.pluginName)
      .title(() => "Apiman")
      .href(() => "/apiman")
      .subPath("Home", "dash.html", builder.join(Apiman.templatePath, 'dash.html'))
      .build();
    builder.configureRouting($routeProvider, tab);
   
    $routeProvider.when('/apiman/consumer-orgs.html',    { templateUrl: builder.join(Apiman.templatePath, 'consumer-orgs.html') });
    $routeProvider.when('/apiman/app-overview.html',     { templateUrl: builder.join(Apiman.templatePath, 'app-overview.html')  });
    $routeProvider.when('/apiman/app-contracts.html',    { templateUrl: builder.join(Apiman.templatePath, 'app-contracts.html') });
    $routeProvider.when('/apiman/dash.html',             { templateUrl: builder.join(Apiman.templatePath, 'dash.html')          });
    $routeProvider.when('/apiman/new-app.html',          { templateUrl: builder.join(Apiman.templatePath, 'new-app.html')       });
    $routeProvider.when('/apiman/new-org.html',          { templateUrl: builder.join(Apiman.templatePath, 'new-org.html')       });
    $routeProvider.when('/apiman/org-plans.html',        { templateUrl: builder.join(Apiman.templatePath, 'org-plans.html')     });
    $routeProvider.when('/apiman/org-services.html',     { templateUrl: builder.join(Apiman.templatePath, 'org-services.html')  });
    $routeProvider.when('/apiman/org-apps.html',         { templateUrl: builder.join(Apiman.templatePath, 'org-apps.html')      });
    $routeProvider.when('/apiman/org-members.html',      { templateUrl: builder.join(Apiman.templatePath, 'org-members.html')   });
    $routeProvider.when('/apiman/plan-overview.html',     { templateUrl: builder.join(Apiman.templatePath, 'plan-overview.html')  });
    $routeProvider.when('/apiman/user-apps.html',        { templateUrl: builder.join(Apiman.templatePath, 'user-apps.html')     });
    $routeProvider.when('/apiman/user-orgs.html',        { templateUrl: builder.join(Apiman.templatePath, 'user-orgs.html')     });
    $routeProvider.when('/apiman/error-409.html',        { templateUrl: builder.join(Apiman.templatePath, 'error-409.html')     });
    $locationProvider.html5Mode(true);
  }]);

  _module.run(['$rootScope','HawtioNav', ($rootScope,HawtioNav:HawtioMainNav.Registry) => {
    HawtioNav.add(tab);
    $rootScope.pluginName = Apiman.pluginName;
    log.debug("loaded");
  }]);


  hawtioPluginLoader.addModule(Apiman.pluginName);
}

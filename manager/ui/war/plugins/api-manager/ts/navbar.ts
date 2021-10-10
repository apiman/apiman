import angular = require("angular");
import { _module } from './apimanPlugin';

_module.controller("Apiman.NavbarController",
    ['$scope', '$rootScope', 'Logger', 'Configuration', 'NotificationService',
      function ($scope, $rootScope, Logger, Configuration, NotificationService) {

      Logger.log("Current user is {0}.", Configuration.user.username);
      $scope.username = Configuration.user.username;
      $scope.logoutUrl = Configuration.apiman.logoutUrl;
      $scope.goBack = function () {
        Logger.info('Returning to parent UI: {0}', Configuration.ui.backToConsole);
        window.location.href = Configuration.ui.backToConsole;
      };
      $scope.userNotificationCount = null;

      NotificationService.getNotificationCount(Configuration.user.username).then(
          (count) => {
            $scope.userNotificationCount = count;
            Logger.info("Unread notifications = {0}", count);
          },
          (failure) => {
            if (failure == null) {
              Logger.error("Probably could not access Total-Count header due to CORS configuration issues when accessing Server API.")
            } else {
              Logger.error("Problem retrieving notifications count {0}", failure);
            }
          } // TODO do something useful
      );

      angular.element(document).ready(function () {
        // Make header not scrollable
        angular.element('html').addClass('layout-pf layout-pf-fixed');

        // Add class "active" based on current href/url
        let path = window.location.pathname;

        // Match dev server and tomcat
        path = path.replace(/^\/apimanui\/|^\/|\/$/g, "");
        path = decodeURIComponent(path);

        angular.forEach(angular.element(".list-group-item a"), function (value) {
          let item = angular.element(value);
          let href = item.attr("href");
          if (path === href) {
            item.closest('li').addClass('active');
            item.closest('.secondary-nav-item-pf').addClass('active');
          }
        })
      });

      $scope.toggleNavBar = function () {
        if ($rootScope.toggle == true) {
          angular.element('.nav-pf-vertical').addClass('collapsed');
          angular.element('.container-pf-nav-pf-vertical').addClass('collapsed-nav');
        } else {
          angular.element('.nav-pf-vertical').removeClass('collapsed');
          angular.element('.container-pf-nav-pf-vertical').removeClass('collapsed-nav');
        }
      };

      $scope.userToggle = function () {
        $rootScope.toggle = ($rootScope.toggle != true);
        $scope.toggleNavBar();
      };

      $scope.onMouseEnter = function (event) {
        angular.element('.nav-pf-vertical').addClass('hover-secondary-nav-pf');
        let item = angular.element(event.target);
        item.closest('li').addClass('is-hover');
      };

      $scope.onMouseLeave = function () {
        angular.element('.nav-pf-vertical').removeClass('hover-secondary-nav-pf');
        // remove the class "is-hover" from whole document so we can't have more than one sub vertical menu
        angular.element('.is-hover').removeClass('is-hover');
      };
    }]);

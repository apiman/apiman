/// <reference path="angular.d.ts"/>
/// <reference path="angular-route.d.ts"/>
/// <reference path="logger.d.ts"/>
declare module Hawtio {

  export interface PluginLoaderStatic {

    /**
     * Register a function to be executed after scripts are loaded but
     * before the app is bootstrapped.
     *
     * 'task' can either be a simple function or an object with the
     * following attributes:
     *
     * name: the task name
     * depends: an array of task names this task needs to have executed first
     * task: the function to be executed with 1 argument, which is a function
     *       that will execute the next task in the queue
     */
    registerPreBootstrapTask(task:any, front?:boolean);

    /**
     * Add an angular module to the list of modules to bootstrap
     */
    addModule(module:string);

    /**
     * Add a URL for discovering plugins.
     */
    addUrl(url:string);

    /**
     * Return the current list of configured modules
     */
    getModules():string[];

    /**
     * Set a callback to be notified as URLs are checked and plugin 
     * scripts are downloaded
     */
    setLoaderCallback(callback:any);

    /**
     * Downloads plugins at any configured URLs and bootstraps the app
     */
    loadPlugins(callback: () => void);

    /**
     * Dumps the current list of configured modules and URLs to the console
     */
    debug();
  }

}

declare var hawtioPluginLoader: Hawtio.PluginLoaderStatic;

declare module HawtioCore {
    /**
     * The app's injector, set once bootstrap is completed
     */
    var injector: ng.auto.IInjectorService;
    /**
     * This plugin's name and angular module
     */
    var pluginName: string;
    /**
     * Dummy local storage object
     */
    var dummyLocalStorage:WindowLocalStorage;

}




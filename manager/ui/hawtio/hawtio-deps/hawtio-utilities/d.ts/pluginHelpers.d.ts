/// <reference path="includes.d.ts" />
/// <reference path="urlHelpers.d.ts" />
declare module PluginHelpers {
    interface PluginModule {
        pluginName: string;
        log: Logging.Logger;
        _module: ng.IModule;
        controller?: (name: string, inlineAnnotatedConstructor: any[]) => any;
    }
    function createControllerFunction(_module: ng.IModule, pluginName: string): (name: string, inlineAnnotatedConstructor: any[]) => ng.IModule;
    function createRoutingFunction(templateUrl: string): (templateName: string, reloadOnSearch?: boolean) => {
        templateUrl: string;
        reloadOnSearch: boolean;
    };
}

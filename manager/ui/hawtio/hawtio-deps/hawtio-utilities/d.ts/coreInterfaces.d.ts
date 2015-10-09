/// <reference path="includes.d.ts" />
/// <reference path="stringHelpers.d.ts" />
declare module Core {
    /**
     * Typescript interface that represents the UserDetails service
     */
    interface UserDetails {
        username: String;
        password: String;
        loginDetails?: Object;
    }
    /**
     * Typescript interface that represents the options needed to connect to another JVM
     */
    interface ConnectToServerOptions {
        scheme: String;
        host?: String;
        port?: Number;
        path?: String;
        useProxy: boolean;
        jolokiaUrl?: String;
        userName: String;
        password: String;
        view: String;
        name: String;
    }
    /**
     * Shorter name, less typing :-)
     */
    interface ConnectOptions extends ConnectToServerOptions {
    }
    interface ConnectionMap {
        [name: string]: ConnectOptions;
    }
    /**
     * Factory to create an instance of ConnectToServerOptions
     * @returns {ConnectToServerOptions}
     */
    function createConnectToServerOptions(options?: any): ConnectToServerOptions;
    function createConnectOptions(options?: any): ConnectOptions;
}

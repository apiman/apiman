import * as jsLogger from "js-logger";
import { ILogger } from "js-logger";

export class ApimanGlobals {
    public static pluginName: string = 'api-manager';
    public static Logger: ILogger = jsLogger.get('apiman-manager');
    public static templatePath: string = 'plugins/api-manager/html';
}

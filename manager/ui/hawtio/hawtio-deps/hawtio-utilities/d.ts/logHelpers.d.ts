/// <reference path="includes.d.ts" />
declare module Log {
    function formatStackTrace(exception: any): string;
    function formatStackLine(line: string): string;
}

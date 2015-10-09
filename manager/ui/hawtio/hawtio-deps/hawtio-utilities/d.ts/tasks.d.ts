/// <reference path="includes.d.ts" />
declare module Core {
    interface Tasks {
        addTask: (name: string, task: () => void) => void;
        execute: () => void;
        reset: () => void;
        onComplete: (cb: () => void) => void;
    }
    interface ParameterizedTasks extends Tasks {
        addTask: (name: string, task: (...params: any[]) => void) => void;
        execute: (...params: any[]) => void;
    }
    interface TaskMap {
        [name: string]: () => void;
    }
    interface ParameterizedTaskMap {
        [name: string]: (...params: any[]) => void;
    }
    class TasksImpl implements Tasks {
        tasks: TaskMap;
        tasksExecuted: boolean;
        _onComplete: () => void;
        addTask(name: string, task: () => void): void;
        private executeTask(name, task);
        onComplete(cb: () => void): void;
        execute(): void;
        reset(): void;
    }
    class ParameterizedTasksImpl extends TasksImpl implements ParameterizedTasks {
        tasks: ParameterizedTaskMap;
        constructor();
        addTask(name: string, task: (...params: any[]) => void): void;
        execute(...params: any[]): void;
    }
    var postLoginTasks: Tasks;
    var preLogoutTasks: Tasks;
    var postLogoutTasks: Tasks;
}

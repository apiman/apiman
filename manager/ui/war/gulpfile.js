// Gulp File

// ---------------------- Dependencies ---->>
const browserify = require('browserify');
const buffer = require('vinyl-buffer');
const eventStream = require('event-stream');
const fs = require('fs');
const gulp = require('gulp');
const map = require('vinyl-map');
const path = require('path');
const pkg = require('./package.json');
const s = require('underscore.string');
const source = require('vinyl-source-stream');

// ---------------------- Gulp Plugins ---->>
const angularTemplatecache = require('gulp-angular-templatecache');
const clean = require('gulp-clean');
const concat = require('gulp-concat');
const concatCss = require('gulp-concat-css');
const connect = require('gulp-connect');
const notify = require('gulp-notify');
const replace = require('gulp-replace');
const runSequence = require('run-sequence');
const typescript = require('gulp-typescript');
const watch = require('gulp-watch');

module.exports = { gulp, connect }


// ---------------------- Configuration ---->>

var config = {
    main: '.',
    ts: 'plugins/**/*.ts',
    templates: 'plugins/**/*.html',
    templateIncludes: 'plugins/**/*.include',
    templateModule: pkg.name + '-templates',
    dest: './dist/',
    js: pkg.name + '.js',
    tsProject: typescript.createProject({
        target: 'ES6',
        module: 'commonjs',
        declarationFiles: true,
        noExternalResolve: false
    })
};

var SwaggerUIPath = './node_modules/swagger-ui-dist';



// ---------------------- Individual Tasks ---->>


// Default Task
// Builds, watches for changes, and spins up the server
gulp.task('default', function() {
    return runSequence('build', 'watch', 'connect');
});



// Browserify Task
// Bundles node_modules required() in ./entry.js to be used on the client (/lib/scripts.js)
gulp.task('browserify', function() {
    return browserify('./entry.js')
        .bundle()
        .pipe(source('deps.js')) // gives streaming vinyl file object
        .pipe(buffer()) // <----- convert from streaming to buffered vinyl file object
        .pipe(gulp.dest(config.dest));
});


// Build Task
gulp.task('build', function() {
    return runSequence(['browserify', 'css', 'fonts', 'images'], 'path-adjust', 'clean-defs', 'tsc', 'template', 'concat', 'clean');
});


// Clean Task
// Cleans the compiled.js and templates.js files
// created in the 'tsc' and 'templates' tasks, respectively
gulp.task('clean', function() {
    return gulp.src(['templates.js', 'compiled.js', 'deps.css'], {read: false})
        .pipe(clean());
});


// Clean-Defs Task
// Cleans, or removes, definition file
gulp.task('clean-defs', function() {
    return gulp.src('defs.d.ts', {read: false})
        .pipe(clean());
});


// Concat Task
// Concatenates two files (compiled.js and templates.js) into ./dist/apiman-manager.js
gulp.task('concat', function() {
    return gulp.src(['compiled.js', 'templates.js'])
        .pipe(concat(config.js))
        .pipe(gulp.dest(config.dest));
});


// Connect Task
gulp.task('connect', function() {
    connect.server({
        root: '.',
        livereload: false,
        port: 2772,
        fallback: 'index.html'
    });
});


// CSS Task
// Concatenates CSS files into one (/lib/styles.css)
gulp.task('css', function() {
    return gulp.src(
        [
            'node_modules/bootstrap-select/dist/css/bootstrap-select.css',
            'node_modules/patternfly/dist/css/patternfly.css',
            'node_modules/patternfly/dist/css/patternfly-additions.css',
            'node_modules/ng-sortable/dist/ng-sortable.css',
            'node_modules/c3/c3.css',
            'node_modules/angular-xeditable-npm/dist/css/xeditable.css',
            'node_modules/angular-scrollable-table/scrollable-table.css',
            'node_modules/angular-ui-bootstrap/ui-bootstrap-csp.css',
            'node_modules/ui-select/dist/select.css',
            'node_modules/select2/select2.css',
            SwaggerUIPath + '/swagger-ui.css',
            'node_modules/prismjs/themes/prism.css',
            'node_modules/@toast-ui/editor-plugin-code-syntax-highlight/dist/toastui-editor-plugin-code-syntax-highlight.css',
            'node_modules/@toast-ui/editor/dist/toastui-editor.css'
        ], {base: 'node_modules/'})
        .pipe(concatCss('deps.css'))
        .pipe(gulp.dest('.'));
});


// Fonts Task
// Copies all fonts to /lib/fonts
gulp.task('fonts', function() {
    return gulp.src(
        [
            './node_modules/bootstrap/dist/fonts*.{eot,svg,ttf,woff,woff2}',
            './node_modules/font-awesome/fonts/*.{eot,svg,ttf,woff,woff2}',
            './node_modules/patternfly/dist/fonts/*.{eot,svg,ttf,woff,woff2}'
        ])
        .pipe(gulp.dest(config.dest + '/fonts'));
});


// Images Task
// Copies all images to /lib/images
gulp.task('images', function() {
    return gulp.src(
        [
            './node_modules/patternfly/dist/img/*.{png,jpg,gif}'
        ])
        .pipe(gulp.dest(config.dest + '/images'));
});


// Path-Adjust Task
// Adjusts all paths within files, if necessary
gulp.task('path-adjust', function() {
    // All CSS
    return gulp.src(['deps.css'])
        .pipe(replace('bootstrap/dist/fonts/', './fonts/'))
        .pipe(replace('font-awesome/fonts/', './fonts/'))
        .pipe(replace('patternfly/dist/fonts/', './fonts/'))
        .pipe(replace('patternfly/dist/img/', './images/'))

        .pipe(gulp.dest(config.dest));
});



// Reload Task
gulp.task('reload', function() {
    gulp.src('.').pipe(connect.reload());
});


// Template Task
// Creates the templates.js file in the project root (/manager/ui/war)
gulp.task('template', function() {
    return gulp.src(config.templates)
        .pipe(angularTemplatecache({
            filename: 'templates.js',
            root: 'plugins/',
            standalone: true,
            module: config.templateModule
        }))
        .pipe(gulp.dest('.'));
});


// TSC Task
// Compiles TS into JS
// Creates the compiled.js file in the project root (/manager/ui/war)
gulp.task('tsc', function() {
    var cwd = process.cwd();

    // Grab all TypeScript files (controllers, services, directives, etc.)
    // Pipe them into the TypeScript project created above (config.tsProject)
    var tsResult = gulp.src(config.ts)
        .pipe(typescript(config.tsProject))
        .on('error', notify.onError({
            message: '#{ error.message }',
            title: 'Typescript compilation error'
        }));

    // eventStream.merge(argument1, argument2);
    // argument1: Take the result of the TypeScript project (.js).
    // Pipe it into, or concatenate it onto the compiled.js file.
    // Then, pipe that file into the project root.
    // argument2: Take the result of the TypeScript project (.dts).
    // Pipe it into a d.ts. file.
    // Pipe that file into the result of a buffer that gets passed to map();
    return eventStream.merge(
        tsResult.js
            .pipe(concat('compiled.js'))
            .pipe(gulp.dest('.')),
        tsResult.dts
            .pipe(gulp.dest('d.ts'))
    ).pipe(map(function(buf, filename) {
            if (!s.endsWith(filename, 'd.ts')) {
                return buf;
            }

            var relative = path.relative(cwd, filename);
            fs.appendFileSync('defs.d.ts', '/// <reference path="' + relative + '"/>\n');
            return buf;
        }));
});


// Watch Task
// Builds, then watches for changes
gulp.task('watch', function() {
    watch([
        'entry.js',
        config.ts,
        config.templates,
        config.templateIncludes,
        'plugins/api-manager/css/apiman.css',
        'apiman/translations.js'
    ], function() {
        return runSequence(['browserify', 'css', 'fonts', 'images'], 'path-adjust', 'clean-defs', 'tsc', 'template', 'concat', 'clean');
    });
});

    
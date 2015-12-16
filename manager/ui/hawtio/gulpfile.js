// Gulp File

// ---------------------- Dependencies ---->>
var browserify = require('browserify');
var buffer = require('vinyl-buffer');
var eventStream = require('event-stream');
var fs = require('fs');
var gulp = require('gulp');
var map = require('vinyl-map');
var path = require('path');
var pkg = require('./package.json');
var s = require('underscore.string');
var source = require('vinyl-source-stream');


// ---------------------- Gulp Plugins ---->>
var angularTemplatecache = require('gulp-angular-templatecache');
var clean = require('gulp-clean');
var concat = require('gulp-concat');
var concatCss = require('gulp-concat-css');
var connect = require('gulp-connect');
var notify = require('gulp-notify');
var replace = require('gulp-replace');
var runSequence = require('run-sequence');
var tsd = require('gulp-tsd');
var typescript = require('gulp-typescript');
var watch = require('gulp-watch');


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
        target: 'ES5',
        module: 'commonjs',
        declarationFiles: true,
        noExternalResolve: false
    })
};

var SwaggerUIPath = './node_modules/swagger-ui-browserify/node_modules/swagger-ui';



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
            'node_modules/patternfly/components/bootstrap-select/dist/css/bootstrap-select.css',
            'node_modules/patternfly/dist/css/patternfly.css',
            'node_modules/patternfly/dist/css/patternfly-additions.css',
            'node_modules/ng-sortable/dist/ng-sortable.css',
            'node_modules/patternfly/components/cs/c3.css',
            'node_modules/angular-xeditable/dist/css/xeditable.css',
            'node_modules/angular-ui-bootstrap/ui-bootstrap-csp.css',
            SwaggerUIPath + '/dist/css/screen.css',
            SwaggerUIPath + '/dist/css/typography.css'
        ], {base: 'node_modules/'})
        .pipe(concatCss('deps.css'))
        .pipe(gulp.dest('.'));
});


// Fonts Task
// Copies all fonts to /lib/fonts
gulp.task('fonts', function() {
    return gulp.src(
        [
            './node_modules/patternfly/components/bootstrap/dist/fonts/*.{eot,svg,ttf,woff,woff2}',
            './node_modules/patternfly/components/font-awesome/fonts/*.{eot,svg,ttf,woff,woff2}',
            './node_modules/patternfly/dist/fonts/*.{eot,svg,ttf,woff,woff2}',
            SwaggerUIPath + '/dist/fonts/*.{eot,svg,ttf,woff,woff2}'
        ])
        .pipe(gulp.dest(config.dest + '/fonts'));
});


// Images Task
// Copies all images to /lib/images
gulp.task('images', function() {
    return gulp.src(
        [
            './node_modules/patternfly/dist/img/*.{png,jpg,gif}',
            SwaggerUIPath + '/dist/images/*.{png,jpg,gif}'
        ])
        .pipe(gulp.dest(config.dest + '/images'));
});


// Path-Adjust Task
// Adjusts all paths within files, if necessary
gulp.task('path-adjust', function() {
    // All CSS
    return gulp.src(['deps.css'])
        .pipe(replace('patternfly/components/bootstrap/dist/fonts/', './fonts/'))
        .pipe(replace('patternfly/components/font-awesome/fonts/', './fonts/'))
        .pipe(replace('patternfly/dist/fonts/', './fonts/'))
        .pipe(replace('patternfly/dist/img/', './images/'))
        .pipe(replace('swagger-ui-browserify/node_modules/swagger-ui/dist/images/', './images/'))
        .pipe(replace('swagger-ui-browserify/node_modules/swagger-ui/dist/fonts/', './fonts/'))
        .pipe(gulp.dest(config.dest));
});



// Reload Task
gulp.task('reload', function() {
    gulp.src('.').pipe(connect.reload());
});


// Setup Task
// This is the initial task that users run to install TS definitions.
gulp.task('setup', ['tsd']);


// Template Task
// Creates the templates.js file in the project root (/manager/ui/hawtio)
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
// Creates the compiled.js file in the project root (/manager/ui/hawtio)
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


// TypeScript Definition Resolutions
gulp.task('tsd', function(callback) {
    tsd({
        command: 'reinstall',
        config: './tsd.json'
    }, callback);
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
        return runSequence('clean-defs', 'tsc', 'template', 'concat', 'clean');
    });
});

    
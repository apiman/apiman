# Hawtio Based API Manager UI

So you're interested in developing the UI for the apiman API Manager?  Here's 
a quick Getting Started guide for that!

1. Install node.js:  http://nodejs.org/
2. Install some stuff using *npm*
    npm install -g bower gulp slush slush-hawtio-javascript slush-hawtio-typescript typescript
3. Change directory into apiman/manager/ui/apiman-plugin
    cd ~/git/apiman/manager/ui/apiman-plugin
4. Use *npm* and *bower* to pull in all dependencies
    npm install
    bower update
5. Make a copy of *js/configuration.nocache.js-SAMPLE* and name it *js/configuration.nocache.js*
6. Configure *configuration.nocache.js* to work with your apiman server
7. Run gulp
    gulp
8. Point your browser wherver gulp tells you!  For example:
    [15:09:24] Using gulpfile /home/ewittman/git/apiman/manager/ui/apiman-plugin/gulpfile.js
    [15:09:24] Starting 'bower'...
    [15:09:24] Finished 'bower' after 8.45 ms
    [15:09:24] Starting 'path-adjust'...
    <snip>
    [15:09:28] Finished 'default' after 6.97 μs
    [15:09:28] Server started http://localhost:2772
    [15:09:28] LiveReload started on port 35729

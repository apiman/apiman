# Hawtio Based API Manager UI

So you're interested in developing the UI for the apiman API Manager?  Here's 
a quick Getting Started guide for that!

* Install node.js:  http://nodejs.org/
* Install some stuff using *npm*
```
    npm install -g bower gulp slush slush-hawtio-javascript slush-hawtio-typescript typescript
```
* Change directory into apiman/manager/ui/hawtio
```
    cd ~/git/apiman/manager/ui/hawtio
```
* Use *npm* and *bower* to pull in all dependencies
```
    npm install
    bower update
```
* Make a copy of *js/configuration.nocache.js-SAMPLE* and name it *js/configuration.nocache.js*
* Configure *configuration.nocache.js* to work with your apiman server
* Run gulp
```
    gulp
```
* Point your browser wherver gulp tells you!  For example:
```
    [15:09:24] Using gulpfile /home/ewittman/git/apiman/manager/ui/hawtio/gulpfile.js
    [15:09:24] Starting 'bower'...
    [15:09:24] Finished 'bower' after 8.45 ms
    [15:09:24] Starting 'path-adjust'...
    <snip>
    [15:09:28] Finished 'default' after 6.97 μs
    [15:09:28] Server started http://localhost:2772
    [15:09:28] LiveReload started on port 35729
```

## Hacking on hawtio-core

To hack on hawtio-core locally first run:

* `npm -g gulp bower karma wiredep`

* `npm install`

This should take care of also installing bower dependencies, if not:

* `bower install`

Then run the app:

* `gulp`


### Add a js dependency:

* `bower install --save my-awesome-dep`

* `gulp bower`

* commit the changed index.html, bower.json and karma.conf.js


### Release an update:

* Update changelog

* `git tag` [some semver](http://semver.org/)

* `git push --tags`


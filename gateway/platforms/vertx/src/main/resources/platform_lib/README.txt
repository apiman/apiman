If you are using fatjars and you want override the default langs.properties, cluster.xml or any other config for the Vert.x platform (i.e.
not for the module!) then you can add them in here and they will be added to the platform classpath when running
your module using Gradle.

The fatjar starter knows to add this directory (and any jars/zips inside it) to the Vert.x platform classpath when executing your module as a fatjar.
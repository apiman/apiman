## Workflows

### `docker-test-release`

Reusable workflow that can build and release Docker images. 

It is called from several other workflows.

If new images are added to the repository, then this workflow should be updated. 

Callers can decide which Apiman version to use, and whether to push the images to the Docker repos, hence this workflow can be in a 'build-only' mode if desired. 

### `update-release-version`

Receives a repository dispatch event from the main Apiman repository containing a new version. This is typically used after a release.

* It writes the new release version to the file `.github/workflows/RELEASE_VERSION`
* Commits the new version
* Tags the commit with the version
* Creates a new GitHub release

### `update-snapshot-version`

Receives a repository dispatch event from the main Apiman repository containing a new snapshot version. 

* It writes the new snapshot version to the file `.github/workflows/SNAPSHOT_VERSION`
* Commits the new version 

### `trigger-release-from-file`

Watches for changes to `.github/workflows/RELEASE_VERSION`, then:

* Calls `docker-test-release`
    * Reads release version 
    * Does a build-only run
    * Does a build & publish run

### `trigger-snapshot-from-file`

Watches for changes to `.github/workflows/SNAPSHOT_VERSION`, then:

* Calls `docker-test-release`
    * Reads snapshot version
    * Does a build-only run
    * Does a build & publish run

### `pull-request-verify`

For PRs:

* Calls `docker-test-release`
    * Reads release version
    * Does a build-only run

* Calls `docker-test-release` (again)
    * Reads snapshot version
    * Does a build-only run

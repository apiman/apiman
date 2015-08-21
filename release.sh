#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing apiman plugins"
echo "######################################"
echo ""

BRANCH=`git rev-parse --abbrev-ref HEAD`

echo "** Current Branch: $BRANCH **"
echo ""

RELEASE_VERSION=$1
DEV_VERSION=$2

if [ "x$RELEASE_VERSION" = "x" ]
then
  read -p "Release Version: " RELEASE_VERSION
fi

if [ "x$DEV_VERSION" = "x" ]
then
  read -p "New Development Version: " DEV_VERSION
fi

echo "######################################"
echo "Release Version: $RELEASE_VERSION"
echo "Dev Version: $DEV_VERSION"
echo "######################################"

rm -rf ~/.m2/repository/io/apiman/plugins
mvn clean install
STATUS=$?
if [ $STATUS -eq 0 ]; then
  echo "Build success!"
else
  echo "Build failed!"
  exit 1
fi

mvn versions:set -DnewVersion=$RELEASE_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Prepare for release $RELEASE_VERSION"
git push origin $BRANCH

mvn clean install

git tag -a -m "Tagging release $RELEASE_VERSION" apiman-plugins-$RELEASE_VERSION
git push origin apiman-plugins-$RELEASE_VERSION

mvn deploy

mvn versions:set -DnewVersion=$DEV_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Update to next development version: $DEV_VERSION"
git push origin $BRANCH

#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing APIMan"
echo "######################################"
echo ""

BRANCH=`git rev-parse --abbrev-ref HEAD`

echo "** Current Branch: $BRANCH **"
echo ""

read -p "Release Version: " RELEASE_VERSION
read -p "New Development Version: " DEV_VERSION

mvn versions:set -DnewVersion=$RELEASE_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Prepare for release $RELEASE_VERSION"
git push origin $BRANCH

mvn clean install

git tag -a -m "Tagging release $RELEASE_VERSION" apiman-$RELEASE_VERSION
git push origin apiman-$RELEASE_VERSION

mvn deploy

mvn versions:set -DnewVersion=$DEV_VERSION
find . -name '*.versionsBackup' -exec rm -f {} \;
git add .
git commit -m "Update to next development version: $DEV_VERSION"
git push origin $BRANCH

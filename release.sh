#!/bin/sh
echo ""
echo "######################################"
echo "  Releasing apiman plugin registry"
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

jq ".plugins[].version = \"$RELEASE_VERSION\" | .version = \"$RELEASE_VERSION\"" registry.json > tmp.json
mv tmp.json registry.json
git add .
git commit -m "Prepare for release $RELEASE_VERSION"
git push origin $BRANCH
git tag -a -m "Tagging release $RELEASE_VERSION" $RELEASE_VERSION
git push origin $RELEASE_VERSION

jq ".plugins[].version = \"$RELEASE_VERSION\" | .version = \"$RELEASE_VERSION\"" registry.json > tmp.json
mv tmp.json registry.json
git add .
git commit -m "Update to next development version: $DEV_VERSION"
git push origin $BRANCH

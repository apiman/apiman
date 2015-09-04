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

sed -i -r "s/\"version\".?:.?\".*\"/\"version\" : \"$RELEASE_VERSION\"/g" registry.json

echo git add .
echo git commit -m "Prepare for release $RELEASE_VERSION"
echo git push origin $BRANCH
echo git tag -a -m "Tagging release $RELEASE_VERSION" $RELEASE_VERSION
echo git push origin $RELEASE_VERSION

sed -i -r "s/\"version\".?:.?\".*\"/\"version\" : \"$DEV_VERSION\"/g" registry.json
echo git add .
echo git commit -m "Update to next development version: $DEV_VERSION"
echo git push origin $BRANCH

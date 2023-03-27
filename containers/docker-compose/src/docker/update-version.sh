#!/bin/bash
set -x

export NEW_RELEASE
export OLD_RELEASE

while getopts "n:o:" opt; do
  case $opt in
    n) NEW_RELEASE="$OPTARG"
    ;;
    o) OLD_RELEASE="$OPTARG"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2
    exit 1
    ;;
  esac

  case $OPTARG in
    -*) echo "Option $opt needs a valid argument"
    exit 1
    ;;
  esac
done

SED=

if command -v gsed &> /dev/null
then
  SED=gsed
else
  # shellcheck disable=SC2209
  SED=sed
fi

$SED -E -i "s|$OLD_RELEASE|$NEW_RELEASE|g" docker-compose.yml
$SED -E -i "s|$OLD_RELEASE|$NEW_RELEASE|g" .env

if [[ $NEW_RELEASE =~ "SNAPSHOT" ]]
then
  $SED -E -i "s|@.*/catalog.json|@master/catalog.json|g" data/apiman.properties
  $SED -E -i "s|@.*/registry.json|@master/registry.json|g" data/apiman.properties
else
  $SED -E -i "s|@.*/catalog.json|@$NEW_RELEASE/catalog.json|g" data/apiman.properties
  $SED -E -i "s|@.*/registry.json|@$NEW_RELEASE/registry.json|g" data/apiman.properties
fi


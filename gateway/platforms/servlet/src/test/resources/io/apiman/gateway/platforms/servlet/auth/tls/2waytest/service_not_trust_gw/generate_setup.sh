#!/bin/bash
set -e
set -x

RSA_KEY_SIZE=4096

skeletonKs() {
  keytool -genkey -alias "$1" -keyalg RSA -sigalg SHA256withRSA -keysize $RSA_KEY_SIZE -storetype JKS \
    -keystore "$1"_ks.jks -validity 99999 -dname "CN=apiman-$1.local, OU=Apiman, O=Apiman Ltd, L=Apiman, ST=, C=GB" \
    -storepass changeme -keypass changeme -noprompt

  keytool -export -alias "$1" -file "$1.cer" -keystore "$1_ks.jks" -storepass changeme -noprompt
}

importCertIntoTs() {
  keytool -import -alias "$2" -trustcacerts -file "$2.cer" -keystore "$1_ts.jks" -storepass changeme -noprompt
}

# Generate service KS, etc.
rm -fv ./*.jks || true

skeletonKs gateway
skeletonKs service

# Just to trigger creation of a trust store, add self as way to make service_ts.jks, etc.
# Does NOT contain the counterparty certificate so we can test this failure scenario
importCertIntoTs service service || true
importCertIntoTs gateway gateway || true

echo "Password is changeme"
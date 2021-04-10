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

addExtraKeyToKs() {
  keytool -genkey -alias "$2" -keyalg RSA -sigalg SHA256withRSA -keysize $RSA_KEY_SIZE -storetype JKS \
    -keystore "$1"_ks.jks -validity 99999 -dname "CN=apiman-$2.local, OU=Apiman, O=Apiman Ltd, L=Apiman, ST=, C=GB" \
    -storepass changeme -keypass changeme -noprompt

  keytool -export -alias "$2" -file "$2.cer" -keystore "$1_ks.jks" -storepass changeme -noprompt
}

importCertIntoTs() {
  keytool -import -alias "$2" -trustcacerts -file "$2.cer" -keystore "$1_ts.jks" -storepass changeme -noprompt
}

# Generate service KS, etc.
rm -fv ./*.jks || true

# Create a keystore for gateway and service
skeletonKs gateway
skeletonKs service

# Add an extra key to the gateway with alias 'gatewayalias'
addExtraKeyToKs gateway gatewayalias

importCertIntoTs service gateway
importCertIntoTs service gatewayalias
importCertIntoTs gateway service

echo "Password is changeme"
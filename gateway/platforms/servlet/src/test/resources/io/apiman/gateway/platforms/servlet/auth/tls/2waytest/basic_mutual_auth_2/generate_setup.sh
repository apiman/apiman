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
rm -fv ./*.jks

skeletonKs gateway
skeletonKs gateway2
skeletonKs service
skeletonKs unrelated

importCertIntoTs service gateway
importCertIntoTs service gateway2
importCertIntoTs gateway service

# One-off import where gateway_ks.jks contains gateway and gateway2 keys.
keytool -import -alias gateway2 -trustcacerts -file "gateway.cer" -keystore "gateway_ks.jks" -storepass changeme -noprompt

echo "Password is changeme"
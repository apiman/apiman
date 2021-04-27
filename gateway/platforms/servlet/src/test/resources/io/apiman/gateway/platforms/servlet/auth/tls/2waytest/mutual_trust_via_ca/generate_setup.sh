#!/bin/bash
set -e
set -x

RSA_KEY_SIZE=4096

# Generate root CA. 
echo "Use the password 'changeme' (without quotes) everywhere it prompts. Different openssl versions
      and shims behaved differently WRT passwords. Also, type 'yes' for trusting certificates."

openssl genrsa -out rootCA.key $RSA_KEY_SIZE
openssl req -x509 -new -sha256 -nodes -key rootCA.key -days 9999 -out rootCA.crt -subj "/C=GB/CN=apimanCA.local"

# Generate service KS, etc.
rm -fv ./*.jks

keytool -genkey -alias service -keyalg RSA -sigalg SHA256withRSA -keysize $RSA_KEY_SIZE -storetype JKS \
  -keystore service_ks.jks -validity 99999 -dname "CN=apiman.local, OU=Apiman, O=Apiman Ltd, L=Apiman, ST=, C=GB" \
  -storepass changeme -keypass changeme

keytool -genkey -alias gateway -keyalg RSA -sigalg SHA256withRSA -keysize $RSA_KEY_SIZE -storetype JKS \
  -keystore gateway_ks.jks -validity 99999 -dname "CN=apiman.local, OU=Apiman, O=Apiman Ltd, L=Apiman, ST=, C=GB" \
  -storepass changeme -keypass changeme

# Import the root CA so we have a common trust of the rootCA defined above
keytool -import -trustcacerts -keystore service_ks.jks -storepass changeme -alias rootCA -file rootCA.crt -noprompt
keytool -import -trustcacerts -keystore gateway_ks.jks -storepass changeme -alias rootCA -file rootCA.crt -noprompt
keytool -import -trustcacerts -keystore common_ts.jks -storepass changeme -alias rootCA -file rootCA.crt -noprompt

## Sign service and gateway with rootCA

# Create CSRs
keytool -certreq -alias service -file service.csr \
  -keystore service_ks.jks -storepass changeme \
  -dname "CN=apiman-service.local, OU=Apiman, O=Apiman Ltd, L=Apiman, ST=, C=GB"

keytool -certreq -alias gateway -file gateway.csr \
  -keystore gateway_ks.jks -storepass changeme \
  -dname "CN=apiman-gateway.local, OU=Apiman, O=Apiman Ltd, L=Apiman, ST=, C=GB"

# Sign...
openssl x509 -req -days 9999 -in service.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out service.crt -sha256
openssl x509 -req -days 9999 -in gateway.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out gateway.crt -sha256

# Now import the signed certificate back into the respective keystores
keytool -import -trustcacerts -keystore service_ks.jks -storepass changeme -alias service -file service.crt
keytool -import -trustcacerts -keystore gateway_ks.jks -storepass changeme -alias gateway -file gateway.crt

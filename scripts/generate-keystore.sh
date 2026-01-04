#!/usr/bin/env sh
set -euo pipefail

KEYSTORE_PATH=${1:?Keystore path must be provided}
KEY_ALIAS=${SERVER_SSL_KEY_ALIAS:-server}
STORE_PASSWORD=${SERVER_SSL_KEYSTORE_PASSWORD:-changeit}
VALIDITY=${SERVER_SSL_VALIDITY_DAYS:-3650}

mkdir -p "$(dirname "$KEYSTORE_PATH")"
if [ -f "$KEYSTORE_PATH" ]; then
  echo "Keystore already exists at $KEYSTORE_PATH"
  exit 0
fi

keytool -genkeypair \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE_PATH" \
  -storepass "$STORE_PASSWORD" \
  -keypass "$STORE_PASSWORD" \
  -dname "CN=localhost, OU=Project Manager, O=Example Inc., L=Budapest, ST=Budapest, C=HU" \
  -validity "$VALIDITY"


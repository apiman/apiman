#!/bin/bash
trap finish EXIT

# Need `allow-preset-passphrase` setting for this.
echo "allow-preset-passphrase" >> ~/.gnupg/gpg-agent.conf
systemctl --user stop gpg-agent
systemctl --user start gpg-agent

# Sometimes seems to be a bit slow, so let's wait.
sleep 2

echo "$GPG_PRIVATE_KEY" > /tmp/pk.key

# Keygrip uniquely is a handle onto any keypair in gpg that is algorithm agnostic.
for handle in $(gpg --show-keys --with-keygrip /tmp/pk.key | grep -i keygrip | awk '{ print $3 }')
do
  # gpg-preset-passphrase is not on the path by default. We're assuming there is only one key.
  /usr/lib/gnupg/gpg-preset-passphrase --preset --passphrase "$GPG_PASSPHRASE" "$handle"
done

git config --global commit.gpgsign true
GPG_SHORT_KEY=$(gpg  --with-colons --show-keys /tmp/pk.key | awk -F: '$1 == "sec" {print $5}' | head -n1)
git config --global user.signingkey "$GPG_SHORT_KEY"

function cleanup {
  rm /tmp/pk.key
}

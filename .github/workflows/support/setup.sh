#!/bin/bash

# Set up git user
echo "Setting up default Git User"
git config --global user.name "$GITHUB_ACTOR"
git config --global user.email "${GITHUB_ACTOR}@users.noreply.github.com"

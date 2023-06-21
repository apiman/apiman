#!/bin/bash

#curl -s "https://get.sdkman.io" | bash
#source "$HOME/.sdkman/bin/sdkman-init.sh"
#sdk install mvnd
#source "$HOME/.sdkman/bin/sdkman-init.sh"
##THIS MUST BE AT THE END OF THE FILE FOR SDKMAN TO WORK!!!
#export SDKMAN_DIR="$HOME/.sdkman"; [[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh" >> ~/.bashrc
#export SDKMAN_DIR="$HOME/.sdkman"; [[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh" >> ~/.bash_profile

mvnFunc='mvn-or-mvnw() {
  local dir="$PWD"
  while [[ ! -x "$dir/mvnw" && "$dir" != / ]]; do
    dir="${dir:h}"
  done

  if [[ -x "$dir/mvnw" ]]; then
    echo "Running \`$dir/mvnw\`..." >&2
    "$dir/mvnw" "$@"
    return $?
  fi

  command mvn "$@"
}'

echo $mvnFunc >> ~/.bash_profile
echo $mvnFunc >> ~/.bashrc
alias mvn="mvn-or-mvnw" >> ~/.bash_profile
alias mvn="mvn-or-mvnw" >> ~/.bash_profile

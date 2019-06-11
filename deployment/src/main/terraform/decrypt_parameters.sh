#!/bin/bash

echo "Please ensure the password needed to decrypt the parameters file is in the TF_VAR_PASSWORD environment variable"
echo "This is stored in AWS SSM under 'ais_to_heatmaps_parameters_password'"

openssl enc -aes-256-cbc -md md5 -d -k $TF_VAR_PASSWORD -in parameters.json.enc -out parameters.secret.json

cat <<EOF
     __ __      __                           __           __
  __/ // /_____/ /__  ____________  ______  / /____  ____/ /
 /_  _  __/ __  / _ \/ ___/ ___/ / / / __ \/ __/ _ \/ __  /
/_  _  __/ /_/ /  __/ /__/ /  / /_/ / /_/ / /_/  __/ /_/ /
 /_//_/  \__,_/\___/\___/_/   \__, / .___/\__/\___/\__,_/
                             /____/_/
EOF

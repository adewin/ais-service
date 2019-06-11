#!/bin/bash

echo "Please ensure the password needed to encrypt the parameters file is in the TF_VAR_PASSWORD environment variable"
echo "This is stored in AWS SSM under 'ais_to_heatmaps_parameters_password'"

openssl enc -aes-256-cbc -md md5 -k $TF_VAR_PASSWORD -in parameters.secret.json -out parameters.json.enc

cat <<EOF
     __ __                                   __           __
  __/ // /____  ____  ____________  ______  / /____  ____/ /
 /_  _  __/ _ \/ __ \/ ___/ ___/ / / / __ \/ __/ _ \/ __  /
/_  _  __/  __/ / / / /__/ /  / /_/ / /_/ / /_/  __/ /_/ /
 /_//_/  \___/_/ /_/\___/_/   \__, / .___/\__/\___/\__,_/
                             /____/_/
EOF

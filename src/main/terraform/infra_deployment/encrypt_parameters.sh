#!/bin/bash

echo "Please enter the password needed to encrypt the parameters file"
echo "This is stored in AWS SSM under 'ais_to_heatmaps_parameters_password'"

read -s AIS_TO_HEATMAPS_PARAMETERS_PASSWORD

openssl enc -aes-256-cbc -md md5 -k $AIS_TO_HEATMAPS_PARAMETERS_PASSWORD -in parameters.secret.json -out parameters.json.enc

cat <<EOF
     __ __                                   __           __
  __/ // /____  ____  ____________  ______  / /____  ____/ /
 /_  _  __/ _ \/ __ \/ ___/ ___/ / / / __ \/ __/ _ \/ __  /
/_  _  __/  __/ / / / /__/ /  / /_/ / /_/ / /_/  __/ /_/ /
 /_//_/  \___/_/ /_/\___/_/   \__, / .___/\__/\___/\__,_/
                             /____/_/
EOF

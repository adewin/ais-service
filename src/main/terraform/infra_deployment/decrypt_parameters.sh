#!/bin/bash

echo "Please enter the password needed to decrypt the parameters file"
echo "This is stored in AWS SSM under 'ais_to_heatmaps_parameters_password'"

read -s AIS_TO_HEATMAPS_PARAMETERS_PASSWORD

openssl enc -aes-256-cbc -d -k $AIS_TO_HEATMAPS_PARAMETERS_PASSWORD -in parameters.json.enc -out parameters.secret.json

cat <<EOF
     __ __      __                           __           __
  __/ // /_____/ /__  ____________  ______  / /____  ____/ /
 /_  _  __/ __  / _ \/ ___/ ___/ / / / __ \/ __/ _ \/ __  /
/_  _  __/ /_/ /  __/ /__/ /  / /_/ / /_/ / /_/  __/ /_/ /
 /_//_/  \__,_/\___/\___/_/   \__, / .___/\__/\___/\__,_/
                             /____/_/
EOF

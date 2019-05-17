# Running infra_deployment

To run the full infra deployment (locally):

```
export TF_VAR_PASSWORD=<ais_to_heatmaps_parameters_password>

terraform init

terraform apply
```

To decrypt `parameters.json.enc`:

```
./decrypt_parameters.sh # Supplying ais_to_heatmaps_parameters_password when prompted
```


To encrypt `parameters.secret.json`:

```
./encrypt_parameters.sh # Supplying ais_to_heatmaps_parameters_password when prompted
```
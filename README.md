# ais-service

[![CircleCI](https://circleci.com/gh/UKHO/ais-service.svg?style=svg)](https://circleci.com/gh/UKHO/ais-service)

This is a Spark job to convert AIS data into a GeoTIFF heatmap. It is intended to be run on AWS EMR.

Lambda aws cli update code example:
aws lambda update-function-code --zip-file fileb://emr_lambda.zip --function-name emr_orchestration


CircleCI requires an AWS IAM User to run the terraform so create an IAM User called circleci and give it the following permissions:
* AWSLambdaFullAccess
* IAMFullAccess
* AmazonS3FullAccess
* CloudWatchFullAccess
* AmazonDynamoDBFullAccess
* AmazonElasticMapReduceFullAccess
* AmazonSSMFullAccess
* AmazonEC2ContainerServiceFullAccess
* AmazonSNSFullAccess

We also have created two policies:
* GlueFullAccess
* FullAthenaAccess

Which give CircleCI full access to Athena and Glue

Terraform needs an S3 bucket and a DynamoDB table to manage remote state storage and locking.
Create an S3 bucket called `ais-to-raster-terra-state` and a DynamoDb table called `ais-to-heatmap-terraform-lock-table` with a primary key of `LockID`

## Deployment

To run the full infra deployment (locally):

```
export TF_VAR_PASSWORD=<ais_to_heatmaps_parameters_password>

./gradlew :deployment:terraformPlan :deployment:terraformApply --interactive
```

To decrypt `parameters.json.enc` (Requires the ```TF_VAR_PASSWORD``` environment variable set):

```
./decrypt_parameters.sh
```


To encrypt `parameters.secret.json`: (Requires the ```TF_VAR_PASSWORD``` environment variable set)

```
./encrypt_parameters.sh
```

### Gradle Terraform plugin
This project provides a simple Terraform plugin to Gradle.

#### Terraform Gradle tasks:

**:deployment:terraformClean** - Removes all terraform binaries

**:deployment:terraformInit** - initialises Terraform, *--interactive* prints out the output of the initialisation to Standard out

**:deployment:terraformValidate** - validates terraform templates

**:deployment:terraformDownload** - downloads Terraform so that the plugin can use it

**deployment:terraformPlan** - Runs Terraform ```plan``` to show what has changed (Runs ```:deployment:terraformInit``` and ```:deployment:terraformDownload``` if these have not been run)

**deployment:terraformApply** - Runs Terraform ```apply```, *--autoApprove* is a flag which will override the manual confirm step to confirm the deployment, the purpose of this is for CI/CD (Runs ```:deployment:terraformInit``` and ```:deployment:terraformDownload``` if these have not been run)

## aggregateReports Gradle Task

The `aggregateReports` task has been added to `spark-ais-to-raster/build.gradle.kts`

It collates each projects `build/reports` into one central location so that they can be viewed locally and within CI

# ais-service

[![Build Status](https://ukhogov.visualstudio.com/Pipelines/_apis/build/status/UKHO.ais-service?branchName=master)](https://ukhogov.visualstudio.com/Pipelines/_build/latest?definitionId=69&branchName=master)

An AWS pipeline to convert AIS data into a GeoTIFF heatmap.

## Deployment

Cloud infrastructure on this project is managed via [Terraform](https://www.terraform.io/).

Terraform needs an S3 bucket (called `ais-to-raster-terra-state`) and a DynamoDB table (`ais-to-heatmap-terraform-lock-table` with a primary key of `LockID`) to manage remote state storage and locking.

To deploy from your local machine, you must export `TF_VAR_PASSWORD` in your shell (i.e. in your `.bashrc`), and run:

```
$ ./gradlew terraformPlan --interactive   # print a 'plan' which shows what changes will be made upon `terraformApply`
$ ./gradlew terraformApply --interactive  # accept the Terraform plan
```

### Azure Pipelines

This project uses Azure Pipelines for both _Continous Integration (CI)_ and _Continous Deployment (CD)_.

Azure Pipelines requires an AWS IAM User to deploy with the following permissions:

* AWSLambdaFullAccess
* IAMFullAccess
* AmazonS3FullAccess
* CloudWatchFullAccess
* AmazonDynamoDBFullAccess
* AmazonElasticMapReduceFullAccess
* AmazonSSMFullAccess
* AmazonEC2ContainerServiceFullAccess
* AmazonSNSFullAccess

### Terraform Gradle tasks

This project provides a simple Terraform plugin to Gradle.

* `:deployment:terraformClean` - delete downloaded Terraform binaries
* `:deployment:terraformInit` - initialise Terraform, use with the `--interactive` flag to print out the output to `stdout`
* `:deployment:terraformValidate` - validate Terraform templates
* `:deployment:terraformDownload` - download a Terraform binary
* `:deployment:terraformPlan` - run Terraform `plan` to show what has changed (depends upon `:deployment:terraformInit` and `:deployment:terraformDownload`)
* `:deployment:terraformApply` - run Terraform `apply`, use `--interactive` to allow confirmation, or (for CI/CD purposes) `--autoApprove` (depends upon `:deployment:terraformInit` and `:deployment:terraformDownload`)

## Running heatmap generation via step function console

To orchestrate the generation of heatmaps with both combinations of resampling parameters, a step function is used which passes the `year`, `month`, `prefix` and `output` parameters through to the Batch job.

The parameters are specified as key-value in JSON, for example:

```json
{
  "year": "",
  "month": "",
  "prefix": "",
  "output": ""
}
```

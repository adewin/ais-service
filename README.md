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

To orchestrate the generation of heatmaps with both combinations of resampling parameters, a step function is used which passes the `filter_sql_file`, `year`, `month`, `prefix` and `output` parameters through to the Batch job.

The parameters are specified as key-value in JSON, for example:

```json
{
  "year": "",
  "month": "",
  "prefix": "",
  "output": "",
  "filter_sql_file": ""
}
```

## Deploying and updating the Batch compute environment

Due to Terraform limitations the Batch Compute Environment and Queue have been created manually using CloudFormation.
Complete CloudFormation documentation can be found here: [AWS CloudFormation Docs](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/Welcome.html)

The CloudFormation template is available from  [./deployment/src/cloudformation/batch-compute-cfn.yml](./deployment/src/cloudformation/batch-compute-cfn.yml)

### To deploy the Cloudformation

1. Go to the AWS Console online and login to the account that the environment is to be deployed into
2. Select 'Cloudformation' from the list of Services
3. If the stack ```manual-batch-compute-environment``` exists then these have been created and if there are any updates to be
applied then the stack needs to be updated in place

    1) Select the stack and click the update button
    2) If the template has changed select replace current template and use the file picker to select the file
    3) Navigate through the wizard to apply the changes, ensure you check the checkbox acknowledging it is creating/updating IAM resources

4. Otherwise click 'Create stack'

    1) Give the stack the name ```manual-batch-compute-environment``` and select the file [./deployment/src/cloudformation/batch-compute-cfn.yml](./deployment/src/cloudformation/batch-compute-cfn.yml)
    as the template
    2) Give values for the parameters

        * ```ResourceNamePrefix``` - Prefix to give to the resources, should be ```manual```
        * ```SecurityGroupId``` - Default Security Group
        * ```Subnets``` - Select all the subnets available
    3) Navigate through the wizard to apply the changes, ensure you check the checkbox acknowledging it is creating/updating IAM resources

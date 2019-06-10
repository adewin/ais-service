# ais-to-raster

[![CircleCI](https://circleci.com/gh/UKHO/spark-ais-to-raster.svg?style=svg)](https://circleci.com/gh/UKHO/spark-ais-to-raster)

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

Terraform needs an S3 bucket and a DynamoDB table to manage remote state storage and locking.
Create an S3 bucket called `ais-to-raster-terra-state` and a DynamoDb table called `ais-to-heatmap-terraform-lock-table` with a primary key of `LockID`


# Data Ingest

For filling out the appropriate minimum viable metadata, you're required to have the start and end datetimes of the AIS. Unfortunately there is no easy way to do this currently, therefore the script `get_dates.py` under `scripts/` has been provided. This can only currently be run on our VMs.

To run the above:

```
python get_dates.py <your-ais-text-file-here>
```

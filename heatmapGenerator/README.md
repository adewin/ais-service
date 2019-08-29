# ais-uk.gov.ukho.ais.heatmaps
[![Build Status](https://ukhogov.visualstudio.com/Pipelines/_apis/build/status/UKHO.ais-heatmap-mob?branchName=master)](https://ukhogov.visualstudio.com/Pipelines/_build/latest?definitionId=119&branchName=master)

Creates Heatmaps from AIS data stored in S3 using AWS Athena.

## Running via docker (locally)

You will need docker installed in a Linux environment

1. Create a .env file with variables

    * OUTPUT_DIRECTORY
    * OUTPUT_FILENAME_PREFIX
    * RESOLUTION
    * RESAMPLING_TIME_THRESHOLD
    * RESAMPLING_DISTANCE_THRESHOLD
    * YEAR
    * MONTH
    * AWS_ACCESS_KEY_ID*
    * AWS_SECRET_ACCESS_KEY*

    _\* Only need 'AWS_ACCESS_KEY_ID' and 'AWS_SECRET_ACCESS_KEY' when running outside of AWS i.e. locally_

2. Build a docker image locally

    ```docker build -t <tag> .```
    
    If you are going to push this image, ensure the tag contains the URL to the repository
3. Run the docker image

    ```docker run --env-file <path to .env file> --name <name to give container> <tag>```

    ```--name <name to give container>```  is optional but makes it easier to reference the container

# process_new_static_file
This lambda processes an input TSV file and appends the filename to the end.

The lambda can be configured from the following environment variables:

* ```DESTINATION_BUCKET``` - The S3 bucket to write the output files to
* ```DESTINATION_KEY_PREFIX```- The prefix to give the output files
* ```OUTPUT_FILE_EXTENSION```- File extension/suffix to give the output files (defaults to '.csv.bz2')
* ```BUFFER_SIZE_MB``` - The number of MB (mega-bytes) to use for the i/o buffer (defaults to 512)

## Run locally
### Activate the local environment
1. Run the ```build``` Gradle task to build the local environment and load the dependencies etc.
2. From a terminal source the '```./build/python/virtualenv/bin/activate```' script

You can use the script: ```./process_new_static_file/run_local.py``` to test the lambda locally.

* ```--bucket``` - the s3 bucket storing the input file
* ```--key``` - the key for the input file in s3
* ```--destination-bucket``` - the name of the s3 bucket to store the output files - overrides environment variable: ```DESTINATION_BUCKET```
* ```--destination-prefix``` - prefix to give the generated object keysoverrides environment variable: ```DESTINATION_KEY_PREFIX```
* ```--output-extension``` - Extension used for the output file (defaults to '.csv.bz2') - overrides environment variable: ```OUTPUT_FILE_EXTENSION```
* ```--buffer-size``` - buffer size (in mb) for buffering read/write (defaults to 256) - overrides environment variable: ```BUFFER_SIZE_MB```

It's easiest to use IntelliJ to run this script.

### Example usage
```bash
./gradlew clean :lambdas:process_new_static_file:build

source ./lambdas/process_new_static_file/build/python/virtualenv/bin/activate

python3 ./lambdas/process_new_static_file/src/main/python/process_new_static_file/run_local.py \
    --bucket $INPUT_BUCKET \
    --key $INPUT_KEY \
    --destination-bucket $DESTINATION_BUCKET \
    --destination-prefix $DESTINATION_PREFIX

deactivate
```

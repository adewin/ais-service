import os


def get():
    return {
        "destinationBucket": os.getenv("DESTINATION_BUCKET", None),
        "destinationKeyPrefix": os.getenv("DESTINATION_KEY_PREFIX", None),
        "outputFileExtension": os.getenv("OUTPUT_FILE_EXTENSION", ".csv.bz2"),
        "bufferSize": int(os.getenv("BUFFER_SIZE_MB", "512")),
    }

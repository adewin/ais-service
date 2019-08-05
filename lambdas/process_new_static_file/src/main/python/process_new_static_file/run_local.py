import argparse
import os
from timeit import timeit

from process_new_static_file import lambda_handler


def trigger_lambda():
    arg_parser = argparse.ArgumentParser()
    arg_parser.add_argument("--bucket", required=True)
    arg_parser.add_argument("--key", required=True)
    arg_parser.add_argument("--destination-bucket", required=True)
    arg_parser.add_argument("--destination-prefix", required=False, default="")
    arg_parser.add_argument("--output-extension", required=False, default=".csv.bz2")
    arg_parser.add_argument("--buffer-size", required=False, default="256")
    args = arg_parser.parse_args()
    s3_event = {
        "Records": [
            {"s3": {"bucket": {"name": args.bucket}, "object": {"key": args.key}}}
        ]
    }
    os.environ["DESTINATION_BUCKET"] = args.destination_bucket
    os.environ["DESTINATION_KEY_PREFIX"] = args.destination_prefix
    os.environ["OUTPUT_FILE_EXTENSION"] = args.output_extension
    os.environ["BUFFER_SIZE_MB"] = args.buffer_size
    print(repr(lambda_handler.lambda_handler(s3_event, {})))


if __name__ == "__main__":
    print(timeit(trigger_lambda, number=1))

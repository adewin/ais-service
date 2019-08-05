from process_new_static_file import new_static_file_handler, config


def _convert_to_s3_url(notification):
    return (
        "s3a://"
        + notification.get("s3").get("bucket").get("name")
        + "/"
        + notification.get("s3").get("object").get("key")
    )


def _validate_event(event):
    if len(event["Records"]) != 1:
        raise Exception("Invalid input: notification should have single record")


def lambda_handler(event, context):
    conf = config.get()
    _validate_event(event)

    changed_file = _convert_to_s3_url(event["Records"][0])

    destination_folder_path = (
        "s3a://"
        + conf.get("destinationBucket")
        + "/"
        + conf.get("destinationKeyPrefix")
    )

    new_file_handler = (
        new_static_file_handler.NewFileHandler()
        .with_output_file_extension(conf.get("outputFileExtension"))
        .with_buffer_size(conf.get("bufferSize"))
    )

    return new_file_handler.handle_new_file(changed_file, destination_folder_path)

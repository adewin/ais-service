import unittest
from unittest.mock import patch, Mock

from process_new_static_file.lambda_handler import lambda_handler

testConfig = {
    "destinationBucket": "destination-bucket",
    "destinationKeyPrefix": "data/",
    "outputFileExtension": ".csv",
    "bufferSize": 1024,
}

expected_destination = "s3a://destination-bucket/data/"
expected_error_message = "Invalid input: notification should have single record"


class TestLambdaHandler(unittest.TestCase):
    @patch("process_new_static_file.new_static_file_handler.NewFileHandler")
    @patch("process_new_static_file.config.get")
    def testWhenCalledWith1FileThenFileHandlerCalled(
        self, mock_config_get, mock_file_handler
    ):
        mock_handler = Mock()
        mock_handler.with_output_file_extension.return_value = mock_handler
        mock_handler.with_buffer_size.return_value = mock_handler
        mock_handler.handle_new_file.return_value = {}

        mock_file_handler.return_value = mock_handler
        mock_config_get.return_value = testConfig

        bucket = "test-bucket"
        object_key = "data/my.csv"
        s3_notification = {
            "Records": [
                {"s3": {"bucket": {"name": bucket}, "object": {"key": object_key}}}
            ]
        }
        expected_input_file = "s3a://" + bucket + "/" + object_key

        response = lambda_handler(s3_notification, {})

        self.assertEqual({}, response)
        mock_handler.handle_new_file.assert_called_with(
            expected_input_file, expected_destination
        )
        mock_handler.with_buffer_size.assert_called_with(1024)
        mock_handler.with_output_file_extension.assert_called_with(".csv")

    @patch("process_new_static_file.new_static_file_handler.NewFileHandler")
    def testWhenCalledWithNoFilesThenRaisesException(self, mock_new_file_handler):
        request = {"Records": []}

        try:
            lambda_handler(request, {})
            self.fail("Was expecting lambda handler to raise exception")
        except Exception as e:
            self.assertEqual(str(e), expected_error_message)
        finally:
            mock_new_file_handler.assert_not_called()

    @patch("process_new_static_file.new_static_file_handler.NewFileHandler")
    def testWhenCalledWithMultipleRecordsThenRaisesException(
        self, mock_new_file_handler
    ):
        s3_notification = {
            "Records": [
                {"s3": {"bucket": {"name": "bucket"}, "object": {"key": "object1"}}},
                {"s3": {"bucket": {"name": "bucket"}, "object": {"key": "object2"}}},
            ]
        }

        try:
            lambda_handler(s3_notification, {})
            self.fail("Was expecting lambda handler to raise exception")
        except Exception as e:
            self.assertEqual(str(e), expected_error_message)
        finally:
            mock_new_file_handler.assert_not_called()

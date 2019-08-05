import unittest
from unittest.mock import patch

from process_new_static_file.new_static_file_handler import NewFileHandler


class TestNewStaticFileHandler(unittest.TestCase):
    @patch("uuid.uuid4")
    @patch("smart_open.open")
    def testWhenAbleToReadFileThenFileIsWrittenToOutputDirectory(
        self, mock_open, mock_uuid_4
    ):
        input_file_path = "/tmp/file.csv"
        output_dir_path = "/tmp/out"
        uuid = "iamauuid"
        expected_out_file = output_dir_path + "/" + uuid + ".csv.bz2"
        expected_buffer_size = 512 * 1024 * 1024
        expected_transport_params = dict(buffer_size=expected_buffer_size)

        mock_uuid_4.return_value = uuid

        file_handler = NewFileHandler()

        result = file_handler.handle_new_file(input_file_path, output_dir_path)

        self.assertEqual(result["created_file"], expected_out_file)
        mock_open.assert_any_call(
            expected_out_file, "w", transport_params=expected_transport_params
        )

        mock_open.assert_any_call(
            input_file_path, transport_params=expected_transport_params
        )

    def testWhenLineGivenToProcessLineThenSourceFileAppended(self):
        line = (
            "190408044071524-190408040335559\t2544444\t\\N\t-1\t0025-4\t"
            "-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t-1\t24\t-1\t-1\t\\N\t\\N\t"
            "-1\t2019-04-08 13:00:38\t2019-04-08 13:00:38\t"
            "2019-04-09 00:18:03\t2019-04-09 00:18:03\t5\n"
        )

        file_name = "new_file"

        result = NewFileHandler._process_line(line, file_name)

        self.assertTrue(
            line.strip() in result, "processed line should contain the original line"
        )
        self.assertTrue(
            file_name in result, "processed line should contain the file name"
        )

        self.assertEqual(len(result), len(line) + len(file_name) + 1)

    def testWhenGivenNewBufferSizeThenNumberOfBytesCorrectlyCalculated(self):
        new_buffer_size_mb = 768
        expected_buffer_size = 1024 * 1024 * new_buffer_size_mb

        file_handler = NewFileHandler()

        file_handler.with_buffer_size(new_buffer_size_mb)

        self.assertEqual(file_handler._buffer_size, expected_buffer_size)

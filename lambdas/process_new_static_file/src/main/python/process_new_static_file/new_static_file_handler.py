import smart_open
import uuid


class NewFileHandler:
    def __init__(self):
        self._output_file_extension = ".csv.bz2"
        self._buffer_size = 512 * 1024 * 1024

    def with_output_file_extension(self, output_file_extension):
        self._output_file_extension = output_file_extension
        return self

    def with_buffer_size(self, buffer_size_mb):
        self._buffer_size = buffer_size_mb * 1024 * 1024
        return self

    def handle_new_file(self, source_file_path, destination_file_path):
        output_file = self._generate_output_file_name(destination_file_path)

        transport_params = dict(buffer_size=(self._buffer_size))
        with smart_open.open(
            output_file, "w", transport_params=transport_params
        ) as fout:
            for line in smart_open.open(
                source_file_path, transport_params=transport_params
            ):
                fout.write(NewFileHandler._process_line(line, source_file_path))

        return {"created_file": output_file}

    def _generate_output_file_name(self, destination_file_path):
        return (
            destination_file_path
            + "/"
            + str(uuid.uuid4())
            + self._output_file_extension
        )

    @staticmethod
    def _process_line(line, file_name):
        return line.strip() + "\t" + file_name + "\n"

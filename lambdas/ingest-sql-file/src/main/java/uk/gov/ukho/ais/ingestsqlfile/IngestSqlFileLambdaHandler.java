package uk.gov.ukho.ais.ingestsqlfile;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;

public class IngestSqlFileLambdaHandler
    extends SpringBootRequestHandler<S3Event, CopyToArchiveResult> {}

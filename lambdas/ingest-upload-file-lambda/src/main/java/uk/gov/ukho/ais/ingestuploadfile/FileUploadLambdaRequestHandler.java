package uk.gov.ukho.ais.ingestuploadfile;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import java.util.function.Supplier;
import uk.gov.ukho.ais.ingestuploadfile.service.FileUploadService;
import uk.gov.ukho.ais.ingestuploadfile.service.FileUploadServiceSupplier;

public class FileUploadLambdaRequestHandler implements RequestHandler<S3Event, Boolean> {

  private Supplier<FileUploadService> fileUploadServiceSupplier;

  public FileUploadLambdaRequestHandler() {
    fileUploadServiceSupplier = new FileUploadServiceSupplier();
  }

  @Override
  public Boolean handleRequest(S3Event input, Context context) {
    return fileUploadServiceSupplier.get().copyFiles(input);
  }
}

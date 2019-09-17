package uk.gov.ukho.ais.ingestsqlfile;

public class CopyToArchiveResult {
  private final String fileUri;
  private final boolean success;

  public CopyToArchiveResult(final String fileUri, final boolean success) {
    this.fileUri = fileUri;
    this.success = success;
  }

  public String getFileUri() {
    return fileUri;
  }

  public boolean isSuccess() {
    return success;
  }
}

package uk.gov.ukho.ais.heatmaps.aggregator.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.S3ObjectSummary
import uk.gov.ukho.ais.heatmaps.aggregator.Config
import uk.gov.ukho.ais.heatmaps.aggregator.model.S3File

object S3FileService {

  def retrieveFileList(implicit config: Config,
                       s3Client: AmazonS3): List[S3File] = {
    import collection.JavaConverters._

    s3Client
      .listObjects(config.heatmapsDirectory)
      .getObjectSummaries
      .asScala
      .map { s3ObjectSummary: S3ObjectSummary =>
        S3File(s3ObjectSummary.getKey)
      }
      .toList
  }
}

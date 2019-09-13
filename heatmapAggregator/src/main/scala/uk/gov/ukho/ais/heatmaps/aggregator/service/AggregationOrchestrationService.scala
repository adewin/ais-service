package uk.gov.ukho.ais.heatmaps.aggregator.service

import com.amazonaws.services.s3.AmazonS3
import uk.gov.ukho.ais.heatmaps.aggregator.Config
import uk.gov.ukho.ais.heatmaps.aggregator.filter.MonthlyS3FilesFilter.Filter
import uk.gov.ukho.ais.heatmaps.aggregator.model.{MonthlyS3File, S3File}

object AggregationOrchestrationService {

  def orchestrateAggregation(implicit config: Config,
                             s3Client: AmazonS3): Unit = {
    println(s"Inspecting ${config.heatmapsDirectory} directory")

    val allFilesInDirectory: List[S3File] = S3FileService.retrieveFileList
    val monthlyFiles: List[MonthlyS3File] =
      allFilesInDirectory.filterMonthlyS3Files

    println(
      s"Found ${allFilesInDirectory.size} files of which ${monthlyFiles.size} are monthly heatmaps")

    val aggregationTargets: Map[String, List[String]] =
      AggregationTargetDiscoveryService
        .discoverAggregationTargets(monthlyFiles, allFilesInDirectory)

    println(s"Found ${aggregationTargets.size} output targets for aggregation")

    aggregationTargets.foreach {
      case (outputPath: String, inputPaths: List[String]) =>
        RasterAggregationService.performRasterAggregation(inputPaths,
                                                          outputPath)
    }
  }

}

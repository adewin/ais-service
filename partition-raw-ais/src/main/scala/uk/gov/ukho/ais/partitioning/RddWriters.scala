package uk.gov.ukho.ais.partitioning

import java.io.File

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import uk.gov.ukho.ais.Session

object RddWriters {

  implicit class YearMonthPartitionWriter(rdd: RDD[((Int, Int), Iterable[Row])]) {

    def writeYearMonthPartitions()(implicit config: Config): Unit = {
      rdd.foreach {
        case ((year: Int, month: Int), rows: Iterable[Row]) =>
          val inputFileName: String = new File(config.inputPath).getName

          Session.sparkSession.sparkContext
            .parallelize[Row](rows.toSeq)
            .map { row: Row =>
              row.toSeq.dropRight(2).mkString("\t")
            }
            .saveAsTextFile(
              config.outputDirectory + s"/year=$year/month=$month/$inputFileName",
              classOf[org.apache.hadoop.io.compress.BZip2Codec])
      }
    }
  }

}

package uk.gov.ukho.ais.partitioning

import java.io.File

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

object RddWriters {

  implicit class YearMonthPartitionWriter(rdd: RDD[((Int, Int), Row)]) {

    def writeYearMonthPartitions()(implicit config: Config): Unit = {
      val inputFileName: String = new File(config.inputPath).getName

      rdd.cache().keys.distinct().collect().foreach {
        case (yearKey: Int, monthKey: Int) =>
          rdd
            .filter {
              case (yearMonth: (Int, Int), _: Row) =>
                (yearKey, monthKey) == yearMonth
            }
            .values
            .map(row => row.toSeq.dropRight(2).mkString("\t"))
            .saveAsTextFile(
              s"${config.outputDirectory}/year=$yearKey/month=$monthKey/$inputFileName",
              classOf[org.apache.hadoop.io.compress.BZip2Codec])
      }
    }
  }

}

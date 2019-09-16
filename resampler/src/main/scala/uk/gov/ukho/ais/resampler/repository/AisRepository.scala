package uk.gov.ukho.ais.resampler.repository

import java.sql.{Connection, PreparedStatement, ResultSet}

import javax.sql.DataSource
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.{getLastDayOfPreviousMonth, getNextMonth}

import scala.collection.mutable

class AisRepository(val dataSource: DataSource)(implicit config: Config) {
  val FETCH_BUFFER_SIZE_ONE_MILLION: Int = 1000000
  val DEFAULT_NUMBER_OF_BUCKETS: Int = 31

  def getDistinctYearAndMonthPairsForFiles(
      inputFiles: Seq[String]): Seq[(Int, Int)] = {
    val connection: Connection = dataSource.getConnection()

    val sqlStatement: PreparedStatement = connection.prepareStatement(s"""
         |SELECT DISTINCT year, month FROM "${config.database}"."${config.table}"
         |WHERE input_ais_data_file = '$inputFiles.head'
         """.stripMargin + inputFiles.tail.foreach { file => s"OR input_ais_data_file = '$file'" })

    val results: ResultSet = sqlStatement.executeQuery()

    new Iterator[(Int, Int)] {
      override def hasNext: Boolean = results.next()
      override def next(): (Int, Int) =
        (results.getInt("year"), results.getInt("month"))
    }.toSeq // TODO: !!!
  }

  def getFilteredPingsByDate(year: Int, month: Int): Iterator[Ping] =
    new Iterator[Ping] {
      val connection: Connection = dataSource.getConnection()
      var bucket: Int = 1

      private val sqlStatements: mutable.Queue[PreparedStatement] = {
        val (nextYear, nextMonth) = getNextMonth(year, month)
        val (prevYear, prevMonth, prevDay) =
          getLastDayOfPreviousMonth(year, month)

        mutable.Queue(
          (0 until DEFAULT_NUMBER_OF_BUCKETS)
            .map(bucket => s"""
                              |SELECT *
                              |FROM "${config.database}"."${config.table}"
                              |WHERE (
                              |(year = $year AND month = $month)
                              |OR (year = $nextYear AND month = $nextMonth AND day=1)
                              |OR (year = $prevYear AND month = $prevMonth AND day=$prevDay)
                              |)
                              |AND mod(cast(mmsi as integer), $DEFAULT_NUMBER_OF_BUCKETS) = $bucket
                              |ORDER BY mmsi, acquisition_time
              """.stripMargin)
            .map(sqlStatement => connection.prepareStatement(sqlStatement)): _*)
      }

      println(
        s"prepared SQL statement for year $year, month $month " +
          s"(bucket $bucket of $DEFAULT_NUMBER_OF_BUCKETS)")

      var results: ResultSet = sqlStatements.dequeue().executeQuery()

      private var _hasNext: Boolean = results.next()

      override def hasNext: Boolean = {
        if (!_hasNext) {
          connection.close()
        }

        _hasNext
      }

      override def next(): Ping = {
        val ping = Ping(
          results.getString("arkposid"),
          results.getString("mmsi"),
          results.getTimestamp("acquisition_time"),
          results.getDouble("lon"),
          results.getDouble("lat"),
          results.getString("vessel_class"),
          results.getInt("message_type_id"),
          results.getString("navigational_status"),
          results.getString("rot"),
          results.getString("sog"),
          results.getString("cog"),
          results.getString("true_heading"),
          results.getString("altitude"),
          results.getString("special_manoeuvre"),
          results.getString("radio_status"),
          results.getString("flags"),
          results.getString("input_ais_data_file")
        )

        _hasNext = results.next()

        if (!_hasNext && sqlStatements.nonEmpty) {
          println(
            s"executing SQL query for year $year, month $month " +
              s"(bucket $bucket of $DEFAULT_NUMBER_OF_BUCKETS)...")
          bucket += 1
          results = sqlStatements.dequeue().executeQuery()
          _hasNext = results.next()
        }

        ping
      }
    }
}

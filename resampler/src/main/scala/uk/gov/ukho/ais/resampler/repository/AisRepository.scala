package uk.gov.ukho.ais.resampler.repository

import java.sql.{Connection, PreparedStatement, ResultSet}

import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.{
  getLastDayOfPreviousMonth,
  getNextMonth
}
import javax.sql.DataSource
import org.slf4j.{Logger, LoggerFactory}
import uk.gov.ukho.ais.resampler.Config

import scala.collection.mutable

class AisRepository(val dataSource: DataSource)(implicit config: Config) {
  val FETCH_BUFFER_SIZE_ONE_MILLION: Int = 1000000
  val DEFAULT_NUMBER_OF_BUCKETS: Int = 31

  private val logger: Logger = LoggerFactory.getLogger(classOf[AisRepository])

  def getDistinctYearAndMonthPairsForFile(
      inputFile: String): Iterator[(Int, Int)] = {
    val connection: Connection = dataSource.getConnection()

    val sqlStatement: PreparedStatement = connection.prepareStatement(s"""
         |SELECT DISTINCT year, month FROM "${config.database}"."${config.table}"
         |WHERE input_ais_data_file = '$inputFile'
         """.stripMargin)

    val results: ResultSet = sqlStatement.executeQuery()

    new Iterator[(Int, Int)] {
      override def hasNext: Boolean = results.next()
      override def next(): (Int, Int) =
        (results.getInt("year"), results.getInt("month"))
    }
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
                              |SELECT mmsi, acquisition_time, lat, lon
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

      logger.info(
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
          results.getString("mmsi"),
          results.getTimestamp("acquisition_time"),
          results.getDouble("lon"),
          results.getDouble("lat")
        )

        _hasNext = results.next()

        if (!_hasNext && sqlStatements.nonEmpty) {
          logger.info(
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

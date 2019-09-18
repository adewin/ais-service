package uk.gov.ukho.ais.resampler.repository

import java.sql.{Connection, PreparedStatement, ResultSet}

import javax.sql.DataSource
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.{
  getLastDayOfPreviousMonth,
  getNextMonth
}

import scala.collection.mutable

object AisRepository {

  val FETCH_BUFFER_SIZE_ONE_MILLION: Int = 1000000
  val DEFAULT_NUMBER_OF_BUCKETS: Int = 31

  implicit class AisDataSource(val dataSource: DataSource)(
      implicit config: Config) {
    def getDistinctYearAndMonthPairsForFiles(
        inputFiles: Seq[String]): Seq[(Int, Int)] = {
      val connection: Connection = dataSource.getConnection()

      val sqlStatement: PreparedStatement =
        connection.prepareStatement("""
                                      |SELECT DISTINCT year, month FROM ?
                                      |WHERE input_ais_data_file in (?)
      """.stripMargin)

      sqlStatement.setString(1, s"${config.database}.${config.table}")
      sqlStatement.setArray(
        2,
        connection.createArrayOf("VARCHAR", inputFiles.toArray))

      val results: ResultSet = sqlStatement.executeQuery()

      Iterator
        .continually((results.getInt("year"), results.getInt("month")))
        .takeWhile(_ => results.next())
        .toSeq
    }

    def getFilteredPingsByDate(year: Int, month: Int): Iterator[Ping] =
      new Iterator[Ping] {
        val connection: Connection = dataSource.getConnection()
        var bucket: Int = 1

        private val sqlStatements: mutable.Queue[PreparedStatement] = {
          val (nextYear, nextMonth) = getNextMonth(year, month)
          val (prevYear, prevMonth, prevDay) =
            getLastDayOfPreviousMonth(year, month)

          mutable.Queue((0 until DEFAULT_NUMBER_OF_BUCKETS)
            .map(bucket => s"""
                                |SELECT *
                                |FROM ?
                                |WHERE (
                                |(year = ? AND month = ?)
                                |OR (year = ? AND month = ? AND day = 1)
                                |OR (year = ? AND month = ? AND day = ?)
                                |)
                                |AND mod(cast(mmsi as integer), ?) = ?
                                |ORDER BY mmsi, acquisition_time
              """.stripMargin)
            .map { sqlStatement =>
              val preparedStatement = connection.prepareStatement(sqlStatement)

              preparedStatement.setString(1, s"${config.database}.${config.table}")
              preparedStatement.setInt(2, year)
              preparedStatement.setInt(3, month)
              preparedStatement.setInt(4, nextYear)
              preparedStatement.setInt(5, nextMonth)
              preparedStatement.setInt(6, prevYear)
              preparedStatement.setInt(7, prevMonth)
              preparedStatement.setInt(8, prevDay)
              preparedStatement.setInt(9, DEFAULT_NUMBER_OF_BUCKETS)
              preparedStatement.setInt(10, bucket)

              preparedStatement
            }: _*)
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
}

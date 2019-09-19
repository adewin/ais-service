package uk.gov.ukho.ais.heatmaps.generator.repository

import java.sql.{Connection, PreparedStatement, ResultSet}

import javax.sql.DataSource
import uk.gov.ukho.ais.heatmaps.generator.model.Ping
import uk.gov.ukho.ais.heatmaps.generator.utility.TimeUtilities.{
  getLastDayOfPreviousMonth,
  getNextMonth
}

import scala.collection.mutable

class AisRepository(val dataSource: DataSource) {
  val FETCH_BUFFER_SIZE_ONE_MILLION: Int = 1000000
  val DEFAULT_NUMBER_OF_BUCKETS: Int = 31

  def getFilteredPingsByDate(filterQuery: String,
                             year: Int,
                             month: Int): Iterator[Ping] =
    new Iterator[Ping] {
      val connection: Connection = dataSource.getConnection()
      var bucket: Int = 1

      val sqlStatements: mutable.Queue[PreparedStatement] = {
        val (nextYear, nextMonth) = getNextMonth(year, month)
        val (prevYear, prevMonth, prevDay) =
          getLastDayOfPreviousMonth(year, month)

        mutable.Queue(
          (0 until DEFAULT_NUMBER_OF_BUCKETS)
            .map(bucket => s"""
                              |SELECT mmsi, acquisition_time, lat, lon
                              |FROM ($filterQuery)
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

      println(s"Running query for bucket: $bucket")
      bucket += 1

      var results: ResultSet = sqlStatements.dequeue().executeQuery()

      var _hasNext: Boolean = results.next()

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
          println(s"Running query for bucket: $bucket")
          bucket += 1
          results = sqlStatements.dequeue().executeQuery()
          _hasNext = results.next()
        }

        ping
      }
    }
}

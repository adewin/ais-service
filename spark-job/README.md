Algorithm is implemented according to the following document (using the preferred normalised ping frequency approach as opposed to the compute saving polyline approach)

https://ukho.sharepoint.com/:b:/s/TeamAtlantis/EVpwWIZrAT9LvgHS_YitmzoB_2ICyQ5IqXZGF6j6YiexNg?e=yW0jcd

Outputting the interpolated points as a TSV can be done using:


    interpolatedShipPings.map {
      case shipPing: ShipPing => s"${shipPing.mmsi}\t${FILENAME_TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(shipPing.acquisitionTime))}\t${shipPing.longitude}\t${shipPing.latitude}"
    }
    .saveAsTextFile("interpolated.csv")
      
Run locally with:

    <your-spark-install-dir>/bin/spark-submit --class uk.gov.ukho.ais.rasters.AisToRaster spark-job/build/libs/spark-job-<version>-all.jar -i <ais-file> -l -o . -p monthly-world-1k-Sep-18 -r 0.1 -d 30000 -t 21600000 -s 2018-09-01 -e 2018-09-30

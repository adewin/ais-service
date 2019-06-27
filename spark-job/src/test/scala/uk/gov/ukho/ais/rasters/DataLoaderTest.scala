package uk.gov.ukho.ais.rasters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DataLoaderTest {
  private final val SINGLE_PING_INPUT_PATH: String = "ais_single_ping.txt"
  private final val SINGLE_PING_NO_STATIC_DATA_INPUT_PATH: String =
    "ais_single_ping_no_static.txt"
  private final val OUTPUT_DIR: String = "out"
  private final val DRAUGHT_FILE: String = "draught_data.txt"
  private final val STATIC_DATA_FILE: String = "test_static_data.txt"
  private final val PREFIX: String = "prefix"
  private final val RESOLUTION: String = "1"
  private final val START_PERIOD = "2019-01-01"
  private final val END_PERIOD = "2019-12-31"
  private final val TIME_THRESHOLD: String = "12"
  private final val DISTANCE_THRESHOLD: String = "13"
  private var testConfig: Config = _

  Session.init(true)

  def setConfig(inputFile: String): Unit = {
    testConfig = ConfigParser.parse(
      Array(
        "-i",
        ResourceService.copyFileToFileSystem(inputFile),
        "-o",
        OUTPUT_DIR,
        "-p",
        PREFIX,
        "-r",
        RESOLUTION,
        "-t",
        TIME_THRESHOLD,
        "-d",
        DISTANCE_THRESHOLD,
        "-s",
        START_PERIOD,
        "-e",
        END_PERIOD,
        "--draughtConfigFile",
        ResourceService.copyFileToFileSystem(DRAUGHT_FILE),
        "--staticDataFile",
        ResourceService.copyFileToFileSystem(STATIC_DATA_FILE)
      ))
  }

  @Test
  def whenLoadingAisDataAndAisDataAndStaticDataExistThenDataLoadedAndJoined()
    : Unit = {
    setConfig(SINGLE_PING_INPUT_PATH)

    val result = DataLoader.loadAisData(testConfig)

    assertThat(result.keys.collect())
      .containsExactly("3456793")

    assertThat(result.values.collect())
      .usingFieldByFieldElementComparator()
      .containsExactly(
        ShipPing("3456793", 1546300850000L, -89.9, 179.9, 2.0D, 1))
  }

  @Test
  def whenLoadingAisDataAndAisDataExistWithoutStaticDataThenDataLoadedAndDraughtSetToMinusOne()
    : Unit = {
    setConfig(SINGLE_PING_NO_STATIC_DATA_INPUT_PATH)

    val result = DataLoader.loadAisData(testConfig)

    assertThat(result.keys.collect())
      .containsExactly("5555555")

    assertThat(result.values.collect())
      .usingFieldByFieldElementComparator()
      .containsExactly(
        ShipPing("5555555", 1546473650000L, -88.9, 178.9, -1D, 1))
  }

  @Test
  def whenLoadingDraughtRangeDataThenDraughtDataLoaded(): Unit = {
    setConfig(SINGLE_PING_INPUT_PATH)

    val result = DataLoader.loadVesselDraughtRangeData(testConfig)

    assertThat(result)
      .usingFieldByFieldElementComparator()
      .containsExactly(VesselDraughtRange(0D, 3D),
                       VesselDraughtRange(3D, 7D),
                       VesselDraughtRange(7D, 1000D),
                       VesselDraughtRange(0D, 5D),
                       VesselDraughtRange(5D, 1000D))
  }
}

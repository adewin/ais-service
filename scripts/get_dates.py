import sys
import bz2

def determine_start_and_end_dates_for_ais_file(filepath):
  min_date = "9999-99-99 99:99:99"
  max_date = "0000-00-00 00:00:00"

  with bz2.BZ2File(filepath, "r") as f:
      for line in f:
          datetime = line.split('\t')[2]
          if datetime < min_date:
              min_date = datetime
          if datetime > max_date:
              max_date = datetime

  print("Start date:  " + min_date)
  print("End date:    " + max_date)

if __name__ == "__main__":
  determine_start_and_end_dates_for_ais_file(sys.argv[1])

select mmsi, acquisition_time, lon, lat, year, month, day
from ukho_ais_data.processed_ais_data
where message_type_id in (1,2,3,18,19)
LIMIT 1000000

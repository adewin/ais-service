package uk.gov.ukho.ais

import java.sql.Timestamp

object SchemaTuple {

  type PartitionedAisSchema = (String,
                               String,
                               Timestamp,
                               Double,
                               Double,
/*
                               String,
                               Int,
                               String,
                               String,
                               String,
                               String,
                               String,
                               String,
                               String,
                               String,
                               String,
                               String,
*/
                               Int,
                               Int,
                               Int)
}

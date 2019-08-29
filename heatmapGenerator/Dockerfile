FROM openjdk:8-jdk AS builder

WORKDIR ais-heatmap-mob
COPY ./ ./
RUN ./gradlew clean assemble

FROM openjdk:8-jre

WORKDIR /opt/generate-heatmap/
COPY --from=builder ./ais-heatmap-mob/build/libs/*-all.jar ./uk.gov.ukho.ais.heatmaps-all.jar
RUN chmod +x ./uk.gov.ukho.ais.heatmaps-all.jar
CMD java -jar /opt/generate-heatmap/uk.gov.ukho.ais.heatmaps-all.jar \
    -o $OUTPUT_DIRECTORY \
    -p $OUTPUT_FILENAME_PREFIX \
    -r $RESOLUTION \
    -t $RESAMPLING_TIME_THRESHOLD \
    -d $RESAMPLING_DISTANCE_THRESHOLD \
    -y $YEAR \
    -m $MONTH

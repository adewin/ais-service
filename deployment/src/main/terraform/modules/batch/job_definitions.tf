resource aws_batch_job_definition monthly_heatmap_job_definition {
  name = "monthly_heatmap_job_definition"
  type = "container"
  timeout {
    attempt_duration_seconds = 36000
  }

  container_properties = <<CONTAINER_PROPERTIES
{
    "image": "${var.docker_registry_url}/ais-generate-heatmaps:${var.project_version}",
    "memory": 60000,
    "vcpus": 1,
    "environment": [
        {"name": "JAVA_OPTS", "value": "-Xmx58g"}
    ],
    "command": [
        "-d",
        "Ref::distance_threshold",
        "-t",
        "Ref::time_threshold",
        "-r",
        "Ref::resolution",
        "--year",
        "Ref::year",
        "--month",
        "Ref::month",
        "--output",
        "Ref::output",
        "-f",
        "Ref::filterSqlFile"
    ],
    "volumes": [],
    "environment": [],
    "mountPoints": [],
    "ulimits": [],
    "resourceRequirements": []
}
CONTAINER_PROPERTIES
}

resource aws_batch_job_definition aggregation_heatmap_job_definition {
  name = "aggregation_heatmap_job_definition"
  type = "container"
  timeout {
    attempt_duration_seconds = 36000
  }

  container_properties = <<CONTAINER_PROPERTIES
{
    "image": "${var.docker_registry_url}/ais-aggregate-heatmaps:${var.project_version}",
    "memory": 60000,
    "vcpus": 1,
    "environment": [
        {"name": "JAVA_OPTS", "value": "-Xmx58g"}
    ],
    "command": [
        "-b",
        "Ref::heatmaps_store"
    ],
    "volumes": [],
    "environment": [],
    "mountPoints": [],
    "ulimits": [],
    "resourceRequirements": []
}
CONTAINER_PROPERTIES
}

resource aws_batch_job_definition resampler_job_definition {
  name = "resampler_job_definition"
  type = "container"
  timeout {
    attempt_duration_seconds = 36000
  }

  container_properties = <<CONTAINER_PROPERTIES
{
    "image": "${var.docker_registry_url}/ais-resampler:${var.project_version}",
    "memory": 60000,
    "vcpus": 8,
    "environment": [
        {"name": "JAVA_OPTS", "value": "-Xmx58g"}
    ],
    "command": [
        "-b",
        "Ref::heatmaps_store"
        "-s",
        "${var.ais_athena_database}",
        "-e",
        "Ref::athena_table",
        "-o",
        "${var.ais_resampled_store_name}",
        "-d",
        "Ref::distance_threshold",
        "-t",
        "Ref::time_threshold",
        "-r",
        "Ref::resolution",
        "--output",
        "Ref::output",
        "Ref::files"
    ],
    "volumes": [],
    "environment": [],
    "mountPoints": [],
    "ulimits": [],
    "resourceRequirements": []
}
CONTAINER_PROPERTIES
}

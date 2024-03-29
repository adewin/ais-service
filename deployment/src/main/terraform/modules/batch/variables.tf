variable docker_registry_url {}
variable project_version {}
variable batch_job_queue_id {}
variable validate_job_config_function_id {}
variable step_execution_timeout_seconds {
  default = 12 * 60 * 60
}
variable step_function_timeout_seconds {
  default = 6 * 24 * 60 * 60
}
variable invoke_step_function_jar {}
variable heatmap_job_submission_bucket {}
variable handle_step_function_outcome_function_id {}

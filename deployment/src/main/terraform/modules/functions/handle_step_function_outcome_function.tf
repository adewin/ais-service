module handle_step_function_outcome_function {
  source        = "./function"
  function_code = var.handle_heatmap_outcome_function_jar
  function_environment_variables = {
    JOB_SUBMISSION_BUCKET_NAME = var.heatmap_job_submission_store_name
  }
  function_handler = "uk.gov.ukho.ais.lambda.handleheatmapoutcome.HandleHeatmapOutcomeLambdaHandler"
  function_name    = "HandleHeatmapOutcomeFunction"
  memory           = "512"
}

module handle_step_function_outcome_function_put_object_perms {
  source                       = "./permissions/store/put-objects"
  function_execution_role_name = module.handle_step_function_outcome_function.function_execution_role_name
  function_name                = module.handle_step_function_outcome_function.function_name
  store_name                   = var.heatmap_job_submission_store_name
}

module handle_step_function_outcome_function_delete_object_perms {
  source                       = "./permissions/store/remove-objects"
  function_execution_role_name = module.handle_step_function_outcome_function.function_execution_role_name
  function_name                = module.handle_step_function_outcome_function.function_name
  store_name                   = var.heatmap_job_submission_store_name
}

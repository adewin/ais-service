module heatmap_job_submission_store {
  source     = "./store"
  store_name = var.heatmap_job_submission_store_name
}

module heatmap_job_submission_full_perms {
  source          = "./permissions/full"
  store_id        = module.heatmap_job_submission_store.store_id
  resource_prefix = "HeatmapJobSubmission"
}

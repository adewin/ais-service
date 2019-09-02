variable heatmap_store {}
variable network_subnet_ids {}
variable network_security_group_ids {}
variable docker_registry_url {}
variable project_version {}
variable data_query_access_policy_id {}
variable step_execution_timeout_seconds {
  default = 6 * 60 * 60 * 60
}

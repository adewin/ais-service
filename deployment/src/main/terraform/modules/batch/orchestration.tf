module batch_heatmap_statemachine {
  source     = "./statemachine"
  name       = "heatmap-step-function"
  definition = <<EOF
{
  "Comment": "A state machine that orchestrates heatmap creation",
  "StartAt": "Validate Input",
  "TimeoutSeconds": ${var.step_function_timeout_seconds},
  "States": {
    "Validate Input": {
      "Type": "Task",
      "Resource": "${var.validate_job_config_function_id}",
      "Next": "Is Valid Request",
      "Catch": [{
        "ErrorEquals": ["States.ALL"],
        "ResultPath": "$.validationFailure",
        "Next": "Complete"
      }],
      "Parameters": {
        "jobConfigFile.$": "$.jobConfigFile",
        "executionId.$": "$$.Execution.Id"
      }
    },
    "Is Valid Request": {
      "Type": "Choice",
      "Choices": [{
          "BooleanEquals": false,
          "Variable": "$.jobConfig.success",
          "Next": "Complete"
      }],
      "Default": "Generate Heatmaps"
    },
    "Generate Heatmaps": {
      "Type": "Parallel",
      "Next": "Aggregate Heatmaps",
      "Catch": [{
        "ErrorEquals": ["States.ALL"],
        "ResultPath": "$.heatmapGenerationFailure",
        "Next": "Complete"
      }],
      "ResultPath": "$.generateHeatmaps",
      "Branches": [
        {
          "StartAt": "Submit 6hr/30km Heatmap",
          "States": {
            "Submit 6hr/30km Heatmap": {
              "Type": "Task",
              "TimeoutSeconds": ${var.step_execution_timeout_seconds},
              "End": true,
              "Resource": "arn:aws:states:::batch:submitJob.sync",
              "InputPath": "$.jobConfig.data",
              "ResultPath": "$.create6hr30kmHeatmap",
              "Parameters": {
                "JobName": "Create6hr30kmHeatmap",
                "JobQueue": "${var.batch_job_queue_id}",
                "JobDefinition": "${aws_batch_job_definition.monthly_heatmap_job_definition.arn}",
                "Parameters": {
                  "distance_threshold": "30000",
                  "time_threshold": "${6 * 60 * 60 * 1000}",
                  "resolution": "0.008983031",
                  "year.$": "$.year",
                  "month.$": "$.month",
                  "output.$": "$.output",
                  "filterSqlFile.$": "$.filterSqlFile"
                }
              }
            }
          }
        },
        {
          "StartAt": "Submit 18hr/100km Heatmap",
          "States": {
            "Submit 18hr/100km Heatmap": {
              "Type": "Task",
              "TimeoutSeconds": ${var.step_execution_timeout_seconds},
              "End": true,
              "Resource": "arn:aws:states:::batch:submitJob.sync",
              "InputPath": "$.jobConfig.data",
              "ResultPath": "$.create18hr100kmHeatmap",
              "Parameters": {
                "JobName": "Create18hr100kmHeatmap",
                "JobQueue":  "${var.batch_job_queue_id}",
                "JobDefinition": "${aws_batch_job_definition.monthly_heatmap_job_definition.arn}",
                "Parameters": {
                  "distance_threshold": "100000",
                  "time_threshold": "${18 * 60 * 60 * 1000}",
                  "resolution": "0.008983031",
                  "year.$": "$.year",
                  "month.$": "$.month",
                  "output.$": "$.output",
                  "filterSqlFile.$": "$.filterSqlFile"
                }
              }
            }
          }
        }
      ]
    },
    "Aggregate Heatmaps": {
      "Type": "Task",
      "TimeoutSeconds": ${var.step_execution_timeout_seconds},
      "Resource": "arn:aws:states:::batch:submitJob.sync",
      "InputPath": "$.jobConfig.data",
      "ResultPath": "$.heatmapAggregation",
      "Next": "Complete",
      "Catch": [{
        "ErrorEquals": ["States.ALL"],
        "ResultPath": "$.heatmapAggregationFailure",
        "Next": "Complete"
      }],
      "Parameters": {
        "JobName": "AggregateHeatmaps",
        "JobQueue": "${var.batch_job_queue_id}",
        "JobDefinition": "${aws_batch_job_definition.aggregation_heatmap_job_definition.arn}",
        "Parameters": {
          "heatmaps_store.$": "$.output"
        }
      }
    },
    "Complete": {
      "Type": "Task",
      "Resource": "${var.handle_step_function_outcome_function_id}",
      "ResultPath": "$",
      "Next": "Determine Success",
      "Catch": [{
        "ErrorEquals": ["States.ALL"],
        "ResultPath": "$.checkCompleteFail",
        "Next": "Failure"
      }]
    },
    "Determine Success": {
      "Type": "Choice",
      "Choices": [{
          "StringEquals": "FAILED",
          "Variable": "$.stepFunctionOutcome",
          "Next": "Failure"
      }],
      "Default": "Success"
    },
    "Failure": {
      "Type": "Fail"
    },
    "Success": {
      "Type": "Succeed"
    }
  }
}
EOF
}

module resampler_statemachine {
  source = "./statemachine"
  name = "resampler-step-function"
  definition = <<EOF
{
  "Comment": "A state machine that orchestrates heatmap creation",
  "StartAt": "Resample",
  "TimeoutSeconds": ${var.step_function_timeout_seconds},
  "States": {
    "Resample": {
      "Type": "Parallel",
      "End": true,
      "Branches": [
        {
          "StartAt": "Submit 6hr 30km resample",
          "States": {
            "Submit 6hr 30km resample": {
              "Type": "Task",
              "TimeoutSeconds": ${var.step_execution_timeout_seconds},
              "End": true,
              "Resource": "arn:aws:states:::batch:submitJob.sync",
              "InputPath": "$.jobConfig.data",
              "ResultPath": "$.create6hr30kmHeatmap",
              "Parameters": {
                "JobName": "Create6hr30kmHeatmap",
                "JobQueue": "${var.batch_job_queue_id}",
                "JobDefinition": "${aws_batch_job_definition.resampler_job_definition.arn}",
                "Parameters": {
                  "distance_threshold": "30000",
                  "time_threshold": "${6 * 60 * 60 * 1000}",
                  "athena_table": "${var.ais_6hr_30km_athena_resampled_table}",
                  "resolution": "0.008983031",
                  "output.$": "$.output",
                  "files.$": "$.files"
                }
              }
            }
          }
        },
        {
          "StartAt": "Submit 18hr 100km resample",
          "States": {
            "Submit 18hr 100km resample": {
              "Type": "Task",
              "TimeoutSeconds": ${var.step_execution_timeout_seconds},
              "End": true,
              "Resource": "arn:aws:states:::batch:submitJob.sync",
              "InputPath": "$.jobConfig.data",
              "ResultPath": "$.create18hr100kmHeatmap",
              "Parameters": {
                "JobName": "Create18hr100kmHeatmap",
                "JobQueue":  "${var.batch_job_queue_id}",
                "JobDefinition": "${aws_batch_job_definition.resampler_job_definition.arn}",
                "Parameters": {
                  "distance_threshold": "100000",
                  "time_threshold": "${18 * 60 * 60 * 1000}",
                  "athena_table": "${var.ais_18hr_100km_athena_resampled_table}",
                  "resolution": "0.008983031",
                  "output.$": "$.output",
                  "files.$": "$.files"
                }
              }
            }
          }
        }
      ]
    }
  }
}
EOF
}

module invoke_heatmap_state_machine_function {
  source           = "../functions/function"
  function_code    = var.invoke_step_function_jar
  function_handler = "uk.gov.ukho.ais.invokestepfunction.InvokeStepFunctionLambdaHandler"
  function_name    = "InvokeHeatmapStateMachine"
  function_environment_variables = {
    STEP_FUNCTION_ID = module.batch_heatmap_statemachine.id
  }
  memory = "512"
}

module start_heatmap_statemachine_permissions {
  source                       = "../functions/permissions/start-state-machine-execution"
  function_execution_role_name = module.invoke_heatmap_state_machine_function.function_execution_role_name
  function_name                = module.invoke_heatmap_state_machine_function.function_name
  state_machine_id             = module.batch_heatmap_statemachine.id
}

module new_job_config_submission_trigger {
  source      = "../functions/triggers/storage_file_upload"
  function_id = module.invoke_heatmap_state_machine_function.function_id
  store_name  = var.heatmap_job_submission_bucket
  item_prefix = "submit/"
}

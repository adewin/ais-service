resource aws_iam_role batch_heatmap_step_fn_execution_role {

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "states.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource aws_sfn_state_machine batch_heatmap_step_fn {
  depends_on = [aws_iam_role.batch_heatmap_step_fn_execution_role, aws_batch_job_definition.monthly_heatmap_job_definition]
  name = "heatmap-step-function"
  role_arn = aws_iam_role.batch_heatmap_step_fn_execution_role.arn
  definition = <<EOF
{
  "Comment": "A state machine that orchestrates heatmap creation",
  "StartAt": "Generate Heatmaps",
  "TimeoutSeconds": ${var.step_execution_timeout_seconds + 10},
  "States": {
    "Generate Heatmaps": {
      "Type": "Parallel",
      "End": true,
      "Branches": [
        {
          "StartAt": "Submit 6hr/30km Heatmap",
          "States": {
            "Submit 6hr/30km Heatmap": {
              "Type": "Task",
              "TimeoutSeconds": ${var.step_execution_timeout_seconds},
              "End": true,
              "Resource": "arn:aws:states:::batch:submitJob.sync",
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
                  "prefix.$": "$.prefix",
                  "output.$": "$.output"
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
                  "prefix.$": "$.prefix",
                  "output.$": "$.output"
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

resource aws_iam_policy step_function_policy {
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": [
                "batch:SubmitJob",
                "batch:DescribeJobs",
                "batch:TerminateJob"
            ],
            "Resource": "*",
            "Effect": "Allow"
        },
        {
            "Action": [
                "events:PutTargets",
                "events:PutRule",
                "events:DescribeRule"
            ],
            "Resource": "*",
            "Effect": "Allow"
        }
    ]
}
EOF
}

resource aws_iam_role_policy_attachment step_function_policy_attachment {
  role = aws_iam_role.batch_heatmap_step_fn_execution_role.name
  policy_arn = aws_iam_policy.step_function_policy.arn
}

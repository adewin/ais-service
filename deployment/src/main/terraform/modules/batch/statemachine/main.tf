resource aws_iam_role statemachine_execution_role {

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

resource aws_sfn_state_machine statemachine {
  depends_on = [aws_iam_role.statemachine_execution_role]
  name = var.name
  role_arn = aws_iam_role.statemachine_execution_role.arn
  definition = var.definition
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
              "lambda:InvokeFunction"
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
  role       = aws_iam_role.statemachine_execution_role.name
  policy_arn = aws_iam_policy.step_function_policy.arn
}


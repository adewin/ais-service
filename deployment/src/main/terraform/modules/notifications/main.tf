resource "aws_cloudwatch_event_rule" "console" {
  name        = "capture-aws-emr-status-change"
  description = "Capture each EMR Cluster status change"

  event_pattern = <<EOF
{
  "source": [
    "aws.emr"
  ],
  "detail-type": [
    "EMR Cluster State Change"
  ],
  "detail": {
    "state": [
      "TERMINATED",
      "TERMINATED_WITH_ERRORS"
    ],
    "name": ["AIS Heatmap Cluster"]
  }
}
EOF
}

resource "aws_cloudwatch_event_target" "sns" {
  rule = "${aws_cloudwatch_event_rule.console.name}"
  target_id = "SendToSNS"
  arn = "${aws_sns_topic.emr_status.arn}"
}

resource "aws_sns_topic" "emr_status" {
  name = "aws-emr-changes"
}

resource "aws_sns_topic_policy" "default" {
  arn = "${aws_sns_topic.emr_status.arn}"
  policy = "${data.aws_iam_policy_document.sns_topic_policy.json}"
}

data "aws_iam_policy_document" "sns_topic_policy" {
  statement {
    effect = "Allow"
    actions = ["SNS:Publish"]

    principals {
      type = "Service"
      identifiers = ["events.amazonaws.com"]
    }

    resources = ["${aws_sns_topic.emr_status.arn}"]
  }
}

resource aws_cloudwatch_event_rule function_trigger {
  name_prefix         = "trigger-${var.function_name}"
  schedule_expression = "cron(${var.cron_expression})"
}

resource aws_cloudwatch_event_target function_trigger_target {
  arn  = var.function_id
  rule = aws_cloudwatch_event_rule.function_trigger.name
}

resource aws_lambda_permission function_trigger_permission {
  action        = "lambda:InvokeFunction"
  function_name = var.function_id
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.function_trigger.arn
}

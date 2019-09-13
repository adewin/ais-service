package uk.gov.ukho.ais.invokestepfunction;

import cyclops.control.Option;

public class InvokeStepFunctionResult {

  private final String stepFunctionArn;
  private final Option<String> executionName;
  private final boolean success;

  public InvokeStepFunctionResult(String stepFunctionArn, Option<String> executionName) {
    this.stepFunctionArn = stepFunctionArn;
    this.executionName = executionName;
    this.success = this.executionName.isPresent();
  }

  public String getStepFunctionArn() {
    return stepFunctionArn;
  }

  public Option<String> getExecutionName() {
    return executionName;
  }

  public boolean isSuccess() {
    return success;
  }
}

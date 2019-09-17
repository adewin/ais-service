variable function_name {}

variable function_handler {}

variable function_code {}

variable function_environment_variables {
  type    = map(string)
  default = {}
}

variable runtime { default = "java8" }
variable memory { default = 258 }
variable timeout { default = 303 }

variable "openai_api_key" {
  description = "OpenAI API key for Lambda function"
  type        = string
  sensitive   = true
}

variable "region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

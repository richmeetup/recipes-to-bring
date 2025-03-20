terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

provider "aws" {
  region = var.region
}

resource "aws_api_gateway_rest_api" "parse_recipe" {
  name = "ParseRecipe"

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_resource" "parse_recipe" {
  rest_api_id = aws_api_gateway_rest_api.parse_recipe.id
  parent_id   = aws_api_gateway_rest_api.parse_recipe.root_resource_id
  path_part   = "parse-recipe"
}

resource "aws_api_gateway_method" "parse_recipe_post" {
  rest_api_id   = aws_api_gateway_rest_api.parse_recipe.id
  resource_id   = aws_api_gateway_resource.parse_recipe.id
  http_method   = "POST"
  authorization = "NONE"
  request_parameters = {
    "method.request.querystring.uri" = true
  }
}

resource "aws_api_gateway_method_settings" "parse_recipe_post" {
  rest_api_id = aws_api_gateway_rest_api.parse_recipe.id
  stage_name  = aws_api_gateway_stage.parse_recipe_stage.stage_name
  method_path = "*/*"

  settings {
    logging_level   = "INFO"
    metrics_enabled = true
  }
}

resource "aws_api_gateway_integration" "parse_recipe_lambda" {
  http_method = aws_api_gateway_method.parse_recipe_post.http_method
  rest_api_id = aws_api_gateway_rest_api.parse_recipe.id
  resource_id = aws_api_gateway_resource.parse_recipe.id

  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.parser_endpoint.arn}/invocations"

  depends_on = [aws_lambda_function.parser_endpoint]
}

resource "aws_api_gateway_deployment" "parse_recipe_deployment" {
  rest_api_id = aws_api_gateway_rest_api.parse_recipe.id

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [
    aws_api_gateway_method.parse_recipe_post,
    aws_api_gateway_integration.parse_recipe_lambda
  ]
}

resource "aws_cloudwatch_log_group" "api_gw_logs" {
  name              = "/aws/api-gateway/${aws_api_gateway_rest_api.parse_recipe.name}"
  retention_in_days = 7
}

resource "aws_iam_role" "api_gw_logging_role" {
  name = "APIGatewayLoggingRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "apigateway.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_policy" "api_gw_logging_policy" {
  name        = "APIGatewayLoggingPolicy"
  description = "Allow API Gateway to write logs to CloudWatch"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:DescribeLogGroups",
        "logs:DescribeLogStreams",
        "logs:PutLogEvents",
        "logs:GetLogEvents",
        "logs:FilterLogEvents"
      ]
      Resource = "*"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "api_gw_logging_attachment" {
  role       = aws_iam_role.api_gw_logging_role.name
  policy_arn = aws_iam_policy.api_gw_logging_policy.arn
}

resource "aws_api_gateway_account" "api_gw_account" {
  cloudwatch_role_arn = aws_iam_role.api_gw_logging_role.arn
}

resource "aws_api_gateway_stage" "parse_recipe_stage" {
  rest_api_id   = aws_api_gateway_rest_api.parse_recipe.id
  deployment_id = aws_api_gateway_deployment.parse_recipe_deployment.id
  stage_name    = "devel"

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gw_logs.arn
    format = jsonencode({
      requestId       = "$context.requestId"
      sourceIp        = "$context.identity.sourceIp"
      requestTime     = "$context.requestTime"
      protocol        = "$context.protocol"
      httpMethod      = "$context.httpMethod"
      resourcePath    = "$context.resourcePath"
      responseLatency = "$context.responseLatency"
      status          = "$context.status"
    })
  }
}

resource "aws_lambda_permission" "api_gateway" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.parser_endpoint.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_api_gateway_rest_api.parse_recipe.execution_arn}/*"
}

resource "aws_lambda_function" "parser_endpoint" {
  function_name = "parser-endpoint"

  runtime     = "java21"
  handler     = "lambda.LambdaHandler::handleRequest"
  memory_size = 512
  timeout     = 60

  environment {
    variables = {
      PARSED_PAGES_BUCKET = aws_s3_bucket.public_bucket.bucket
      OPENAI_API_KEY      = var.openai_api_key
    }
  }

  filename         = "${path.module}/../target/scala-2.13/parser-endpoint-assembly-0.1.0-SNAPSHOT.jar"
  source_code_hash = filebase64sha256("${path.module}/../target/scala-2.13/parser-endpoint-assembly-0.1.0-SNAPSHOT.jar")

  role = aws_iam_role.lambda_exec.arn
}

resource "aws_cloudwatch_log_group" "parser_endpoint" {
  name              = "/aws/lambda/${aws_lambda_function.parser_endpoint.function_name}"
  retention_in_days = 14
}

resource "aws_iam_role" "lambda_exec" {
  name = "lambda-exec-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Sid    = ""
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }]
  })
}

resource "aws_s3_bucket" "public_bucket" {
  # XXX - really don't like how this is exposed -- fix this later
  bucket = "parsed-pages"
}

resource "aws_s3_bucket_ownership_controls" "public_bucket_ownership" {
  bucket = resource.aws_s3_bucket.public_bucket.bucket

  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_public_access_block" "public_bucket_access_block" {
  bucket = resource.aws_s3_bucket.public_bucket.bucket

  block_public_acls       = false
  ignore_public_acls      = false
  block_public_policy     = true
  restrict_public_buckets = true
}

resource "aws_iam_role_policy_attachment" "lambda_policy_basic" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_policy" "lambda_s3_policy" {
  name        = "lambda-s3-policy"
  description = "Allow lambda to access S3"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "s3:PutObject",
        "s3:PutObjectAcl"
      ]
      Resource = [
        "${aws_s3_bucket.public_bucket.arn}/*"
      ]
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_policy_s3" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = aws_iam_policy.lambda_s3_policy.arn
}

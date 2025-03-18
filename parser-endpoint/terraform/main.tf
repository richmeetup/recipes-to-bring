terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

resource "aws_lambda_function" "parser_endpoint" {
  function_name = "ParserEndpoint"

  runtime = "java21"
  handler = "lambda.LambdaHandler::handleRequest"

  environment {
    variables = {
      PARSED_PAGES_BUCKET = aws_s3_bucket.public_bucket.bucket
    }
  }

  filename = "${path.module}/../target/scala-2.13/parser-endpoint-assembly-0.1.0-SNAPSHOT.jar"
  source_code_hash = filebase64sha256("${path.module}/../target/scala-2.13/parser-endpoint-assembly-0.1.0-SNAPSHOT.jar")

  role = aws_iam_role.lambda_exec.arn
}

resource "aws_cloudwatch_log_group" "parser_endpoint" {
  name              = "/aws/lambda/${aws_lambda_function.parser_endpoint.function_name}"
  retention_in_days = 14
}

resource "aws_iam_role" "lambda_exec" {
  name = "serverless_lambda"

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
  bucket = "parsed-pages"
}

resource "aws_iam_role_policy_attachment" "lambda_policy" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}
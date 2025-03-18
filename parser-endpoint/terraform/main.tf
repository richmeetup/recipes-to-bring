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
  function_name = "parser-endpoint"

  runtime = "java21"
  handler = "lambda.LambdaHandler::handleRequest"
  memory_size = 512
  timeout = 30

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

  block_public_acls   = false
  ignore_public_acls  = false
  block_public_policy = true
  restrict_public_buckets = true
}

resource "aws_iam_role_policy_attachment" "lambda_policy_basic" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_policy" "lambda_s3_policy" {
  name        = "lambda-s3-policy"
  description = "Allow lambda to access S3"
  policy      = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = [
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
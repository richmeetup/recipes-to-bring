.PHONY: all check-env test build terraform-init terraform-plan terraform-apply deploy

# Required environment variables
REQUIRED_ENV_VARS = AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_REGION TF_VAR_openai_api_key

# Function to check if required env variables exist
check-env:
	@for var in $(REQUIRED_ENV_VARS); do \
		if [ -z "$$${var}" ]; then \
			echo "Error: Missing environment variable: $$var"; \
			exit 1; \
		fi; \
	done
	@echo "All required environment variables are set ✅"
	
# Test 
test: 
	sbt test 

# Build the Scala project
build: test check-env
	sbt assembly

# Initialize Terraform
terraform-init:
	cd terraform && terraform init

# Plan Terraform deployment
terraform-plan:
	cd terraform && terraform plan

# Apply Terraform deployment
terraform-apply:
	cd terraform && terraform apply -auto-approve

# Destroy Terraform deployment
terraform-destroy:
	cd terraform && terraform destroy -auto-approve

# Full deployment pipeline
deploy: build terraform-init terraform-plan terraform-apply
	@echo "Deployment successful 🚀"

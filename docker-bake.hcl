variable "BUILD_NUMBER" {
  default = "0"
}

variable "SPRING_DATASOURCE_URL" {
  default = ""
}

variable "SPRING_DATASOURCE_USERNAME" {
  default = ""
}

variable "SPRING_DATASOURCE_PASSWORD" {
  default = ""
}

variable "APP_SECURITY_JWT_SECRET" {
  default = ""
}

variable "APP_SECURITY_JWT_EXPIRATION_MS" {
  default = "3600000"
}

group "default" {
  targets = ["image"]
}

target "image" {
  context = "./backend"
  dockerfile = "Dockerfile"
  args = {
    BUILD_NUMBER = BUILD_NUMBER
    SPRING_DATASOURCE_URL = SPRING_DATASOURCE_URL
    SPRING_DATASOURCE_USERNAME = SPRING_DATASOURCE_USERNAME
    SPRING_DATASOURCE_PASSWORD = SPRING_DATASOURCE_PASSWORD
    APP_SECURITY_JWT_SECRET = APP_SECURITY_JWT_SECRET
    APP_SECURITY_JWT_EXPIRATION_MS = APP_SECURITY_JWT_EXPIRATION_MS
  }
  tags = [
    "${REGISTRY}/${IMAGE_NAME}:${TAG}",
    "${REGISTRY}/${IMAGE_NAME}:latest"
  ]
  push = true
}

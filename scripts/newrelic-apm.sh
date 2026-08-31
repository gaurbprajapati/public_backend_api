#!/bin/bash

TOKEN=$(curl -sX PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 300")
export AWS_REGION=$(curl -sH "X-aws-ec2-metadata-token: $TOKEN" "http://169.254.169.254/latest/meta-data/placement/region")

case "$AWS_REGION" in
  us-east-1)      AWS_REGION_NAME="Virginia" ;;
  us-east-2)      AWS_REGION_NAME="Ohio" ;;
  us-west-1)      AWS_REGION_NAME="California" ;;
  us-west-2)      AWS_REGION_NAME="Oregon" ;;
  ap-south-1)     AWS_REGION_NAME="Mumbai" ;;
  ap-northeast-1) AWS_REGION_NAME="Tokyo" ;;
  ap-northeast-2) AWS_REGION_NAME="Seoul" ;;
  ap-northeast-3) AWS_REGION_NAME="Osaka" ;;
  ap-southeast-1) AWS_REGION_NAME="Singapore" ;;
  ap-southeast-2) AWS_REGION_NAME="Sydney" ;;
  eu-central-1)   AWS_REGION_NAME="Frankfurt" ;;
  eu-west-1)      AWS_REGION_NAME="Ireland" ;;
  eu-west-2)      AWS_REGION_NAME="London" ;;
  eu-west-3)      AWS_REGION_NAME="Paris" ;;
  sa-east-1)      AWS_REGION_NAME="Sao_Paulo" ;;
  ca-central-1)   AWS_REGION_NAME="Canada" ;;
  *)              AWS_REGION_NAME="$AWS_REGION" ;;  # fallback
esac

export AWS_REGION_NAME
echo "AWS_REGION_NAME: $AWS_REGION_NAME"

set -a
source /tmp/templates/new-relic/newrelic-vars.env
set +a

envsubst < /tmp/templates/new-relic/newrelic.tmpl > /opt/newrelic/newrelic.yml

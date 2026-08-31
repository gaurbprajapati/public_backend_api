#!/bin/bash

# conditional file placements
if grep -q "CUBEAPM_ENABLE=1" /tmp/templates/nr_enabled.env; then
    echo "CubeAPM is enabled"
    sudo mv /tmp/templates/timesheet-service/timesheet-service-nr.service /etc/systemd/system/timesheet-microservice.service
else
    echo "CUBEAPM_ENABLE is disabled"
    sudo mv /tmp/templates/timesheet-service/timesheet-service.service /etc/systemd/system/timesheet-microservice.service
fi

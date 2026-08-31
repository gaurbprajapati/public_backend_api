#!/bin/bash
echo "ApplicationStop stage"
systemctl stop timesheet-microservice.service
systemctl stop apache2

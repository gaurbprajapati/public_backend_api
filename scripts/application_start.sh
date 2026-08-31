#!/bin/bash
echo "ApplicationStart stage"

sudo systemctl daemon-reload
sudo systemctl start timesheet-microservice.service
sudo systemctl start apache2

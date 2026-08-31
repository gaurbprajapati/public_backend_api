#!/bin/bash
echo "AfterInstall stage"

find /var/www/html -type d -exec chmod 755 {} +
find /var/www/html -type f -exec chmod 644 {} +

find /var/www/html -type d -exec chown webapp:webapp {} +
find /var/www/html -type f -exec chown webapp:webapp {} +
sudo systemctl stop logstash
if sudo grep -R "ENABLE_ES_LOGGING=1" /tmp/templates/nr_enabled.env
then
    sudo systemctl restart filebeat
else
    sudo systemctl stop filebeat
fi
if sudo grep -q "CUBEAPM_ENABLE=1" /tmp/templates/nr_enabled.env; 
then
    sudo systemctl restart otelcol-contrib
else
    sudo systemctl stop otelcol-contrib
fi
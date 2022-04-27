#!/bin/bash
ACCESS_KEY=__YOUR_DATA__
SECRET_KEY=__YOUR_DATA__
ENDPOINT=__YOUR_DATA__
s3cmd --host=${ENDPOINT} --host-bucket=${ENDPOINT} --access_key=${ACCESS_KEY} --secret_key=${SECRET_KEY} sync ./config/owl/ s3://mystd-config

#!/bin/sh

set -e

mkdir -p /opt/mystandard/lib/
mkdir -p /opt/mystandard/config/owl/

mv /opt/application.yml /opt/mystandard/config/
mv /opt/application-detail.yml /opt/mystandard/config/
mv /opt/application-env.yml /opt/mystandard/config/
mv /opt/CLV-AP_IT.rdf /opt/mystandard/config/owl/
mv /opt/COV-AP_IT.rdf /opt/mystandard/config/owl/
mv /opt/default_state_machine.xml /opt/mystandard/config/
mv /opt/index_definition.json /opt/mystandard/config/
mv /opt/legal-status.rdf /opt/mystandard/config/owl/
mv /opt/mybox.properties /opt/mystandard/config/
mv /opt/mystd_bpo.owl /opt/mystandard/config/owl/
mv /opt/SM-AP_IT.rdf /opt/mystandard/config/owl/
mv /opt/TI-AP_IT.rdf /opt/mystandard/config/owl/
mv /opt/TSI.owl /opt/mystandard/config/owl/
mv /opt/TSI_data.owl /opt/mystandard/config/owl/

mv /opt/mystandard-1.1.3.jar /opt/mystandard/lib/

#mv /opt/supervisord-rve-mystandard.ini /etc/supervisord.d/
#!/bin/bash

clear
cd ~/conjur-ubuntu-install
./cli-config.sh

vi cybrselfserve.properties
./restart_servers.sh

clear
echo "Rebuilding demo environment..."
./ant.sh publish
echo "Waiting for servlet registration..."
sleep 10

clear
echo "Running liveness tests to validate configuration..."
cd ../servlet-tests
./liveness-test.sh
echo "If you saw values for PAS & Conjur tokens, you are good to go."

cd ../policy-edit
./init-synch.sh

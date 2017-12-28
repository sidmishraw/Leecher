#!/bin/bash
# Author: Sidharth Mishra
# Description: This script starts Leecher

if [[ ("$1"="start") && ("$2"="leeching") && (-n "$3") ]]
then
  java -jar leecher.jar "$3";
else
  echo "Not the right usage";
  echo "leecher start leeching x";
fi
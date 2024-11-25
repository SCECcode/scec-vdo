#!/bin/sh
# Cross-platform solution to get memory in GB on Linux and MacOS
awk '/MemTotal/ {print int($2/1024/1024)}' /proc/meminfo 2>/dev/null || sysctl -n hw.memsize | awk '{print int($1/1024/1024/1024)}'


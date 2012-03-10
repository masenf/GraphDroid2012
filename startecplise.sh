#!/bin/sh
eclipse -vmargs -Xms2048m -Xmx2048m -XX:+UseParallelGC -XX:PermSize=1024M -XX:MaxPermSize=2048M

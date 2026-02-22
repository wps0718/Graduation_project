#!/usr/bin/env bash
mvn test -Dtest=ScheduledTaskTest 2>&1 | tee run-folder/F25-定时任务/test_output.log
mvn compile -q

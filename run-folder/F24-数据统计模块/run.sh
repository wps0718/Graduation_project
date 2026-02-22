#!/usr/bin/env bash
mvn -Dtest=StatsServiceImplTest test 2>&1 | tee run-folder/F24-数据统计模块/test_output.log
mvn compile -q

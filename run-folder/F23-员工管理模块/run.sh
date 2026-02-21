#!/usr/bin/env bash
mvn -Dtest=EmployeeServiceImplTest test 2>&1 | tee run-folder/F23-员工管理模块/test_output.log
mvn compile -q

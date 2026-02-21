#!/bin/bash
mvn test -Dtest=UserServiceImplTest 2>&1 | tee run-folder/F04-用户信息管理/test_output.log

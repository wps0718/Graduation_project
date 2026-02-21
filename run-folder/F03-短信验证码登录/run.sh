#!/bin/bash
mvn clean test -Dtest=UserServiceImplTest 2>&1 | tee run-folder/F03-短信验证码登录/test_output.log

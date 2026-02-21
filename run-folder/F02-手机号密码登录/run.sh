#!/bin/bash
mvn test -Dtest=UserServiceImplTest 2>&1 | tee run-folder/F02-手机号密码登录/test_output.log

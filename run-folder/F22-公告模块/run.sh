#!/usr/bin/env bash
mvn -Dtest=NoticeServiceImplTest test 2>&1 | tee run-folder/F22-公告模块/test_output.log
mvn compile -q

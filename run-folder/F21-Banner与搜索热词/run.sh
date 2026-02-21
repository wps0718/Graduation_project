#!/usr/bin/env bash
mvn -Dtest=BannerServiceImplTest test 2>&1 | tee run-folder/F21-Banner与搜索热词/test_output.log
mvn -Dtest=SearchKeywordServiceImplTest test 2>&1 | tee -a run-folder/F21-Banner与搜索热词/test_output.log
mvn compile -q

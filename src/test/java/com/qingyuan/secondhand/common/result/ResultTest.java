package com.qingyuan.secondhand.common.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void errorWithCodeSetsCodeAndMessage() {
        Result<?> result = Result.error(401, "Unauthorized");
        assertThat(result.getCode()).isEqualTo(401);
        assertThat(result.getMsg()).isEqualTo("Unauthorized");
    }
}

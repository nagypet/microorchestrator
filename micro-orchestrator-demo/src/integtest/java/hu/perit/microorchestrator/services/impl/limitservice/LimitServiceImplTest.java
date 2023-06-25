/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.microorchestrator.services.impl.limitservice;

import hu.perit.microorchestrator.config.Constants;
import hu.perit.microorchestrator.services.api.LimitService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"default", "spvitamin-defaults"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class LimitServiceImplTest
{
    @Autowired
    private LimitService limitService;

    @Test
    void testDecreaseLimit()
    {
        boolean success = this.limitService.decreaseLimit(Constants.BANK_USER_ID, new BigDecimal("12"));
        assertThat(success).isTrue();
        success = this.limitService.decreaseLimit(Constants.BANK_USER_ID, new BigDecimal("1000"));
        assertThat(success).isFalse();
    }

}

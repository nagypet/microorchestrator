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

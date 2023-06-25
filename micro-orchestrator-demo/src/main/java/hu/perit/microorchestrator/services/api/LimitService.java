package hu.perit.microorchestrator.services.api;

import java.math.BigDecimal;

public interface LimitService
{
    boolean checkLimit(Long userId, BigDecimal amount);

    boolean decreaseLimit(Long userId, BigDecimal amount);

    void increaseLimit(Long userId, BigDecimal amount);

    BigDecimal getRemainingLimit(Long userId);
}

package hu.perit.microorchestrator.services.api;

import java.math.BigDecimal;

public interface LimitService
{
    boolean decreaseLimit(Long userId, BigDecimal amount);
    void increaseLimit(Long userId, BigDecimal amount);
}

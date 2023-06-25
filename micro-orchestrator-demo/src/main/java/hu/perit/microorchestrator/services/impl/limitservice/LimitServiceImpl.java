package hu.perit.microorchestrator.services.impl.limitservice;

import hu.perit.microorchestrator.services.api.LimitService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class LimitServiceImpl implements LimitService
{
    public static final BigDecimal MAX_LIMIT = new BigDecimal("1000");

    private final Map<Long, BigDecimal> limits = new HashMap<>();

    @Override
    public boolean checkLimit(Long userId, BigDecimal amount)
    {
        this.limits.putIfAbsent(userId, MAX_LIMIT);
        BigDecimal limit = this.limits.get(userId);
        return limit.compareTo(amount) >= 0;
    }

    @Override
    public boolean decreaseLimit(Long userId, BigDecimal amount)
    {
        this.limits.putIfAbsent(userId, MAX_LIMIT);
        BigDecimal limit = this.limits.get(userId);
        BigDecimal newLimit = limit.subtract(amount);
        if (newLimit.compareTo(BigDecimal.ZERO) < 0)
        {
            return false;
        }
        this.limits.put(userId, newLimit);
        return true;
    }

    @Override
    public void increaseLimit(Long userId, BigDecimal amount)
    {
        if (!this.limits.containsKey(userId))
        {
            return;
        }

        BigDecimal limit = this.limits.get(userId);
        BigDecimal newLimit = limit.add(amount);
        this.limits.put(userId, newLimit);
    }

    @Override
    public BigDecimal getRemainingLimit(Long userId)
    {
        if (!this.limits.containsKey(userId))
        {
            return MAX_LIMIT;
        }

        return this.limits.get(userId);
    }
}

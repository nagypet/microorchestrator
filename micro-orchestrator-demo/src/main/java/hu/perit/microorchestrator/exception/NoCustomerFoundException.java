package hu.perit.microorchestrator.exception;

import org.springframework.security.core.AuthenticationException;

public class NoCustomerFoundException extends AuthenticationException
{
    public NoCustomerFoundException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public NoCustomerFoundException(String msg)
    {
        super(msg);
    }
}

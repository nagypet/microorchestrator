package hu.perit.microorchestrator.services.api;

import hu.perit.spvitamin.spring.auth.AuthorizationToken;

public interface AuthService
{
    AuthorizationToken login();
}

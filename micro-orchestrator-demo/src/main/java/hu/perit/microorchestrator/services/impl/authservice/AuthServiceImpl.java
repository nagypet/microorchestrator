package hu.perit.microorchestrator.services.impl.authservice;

import hu.perit.microorchestrator.services.api.AuthService;
import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import hu.perit.spvitamin.spring.security.auth.jwt.TokenClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService
{
    private final JwtTokenProvider tokenProvider;
    private final AuthorizationService authorizationService;


    @Override
    public AuthorizationToken login()
    {
        AuthenticatedUser authenticatedUser = this.authorizationService.getAuthenticatedUser();

        return tokenProvider.generateToken(authenticatedUser.getUsername(),
                new TokenClaims(authenticatedUser.getUserId(), authenticatedUser.getAuthorities(), authenticatedUser.getLdapUrl()));
    }
}

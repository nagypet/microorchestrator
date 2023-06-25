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

package hu.perit.microorchestrator.auth;

import hu.perit.microorchestrator.exception.NoCustomerFoundException;
import hu.perit.microorchestrator.services.api.CustomerService;
import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Inserts the userId into the AuthenticatedUser
 *
 * @author Peter Nagy
 */

@Slf4j
public class PostAuthenticationFilter extends OncePerRequestFilter
{

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        log.debug("{} called", this.getClass().getName());

        AuthorizationService authorizationService = SpringContext.getBean(AuthorizationService.class);

        AuthenticatedUser authenticatedUser = authorizationService.getAuthenticatedUser();

        if (!authenticatedUser.isAnonymous() && authenticatedUser.getUserId() < 0)
        {
            CustomerService customerService = SpringContext.getBean(CustomerService.class);
            try
            {
                CustomerDto customerDto = customerService.getCustomerByUsername(authenticatedUser.getUsername());
                authenticatedUser.setUserId(customerDto.getId());

                // store in the security context
                authorizationService.setAuthenticatedUser(authenticatedUser);
            }
            catch (ResourceNotFoundException e)
            {
                log.warn(StackTracer.toString(e));
                throw new NoCustomerFoundException(e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}

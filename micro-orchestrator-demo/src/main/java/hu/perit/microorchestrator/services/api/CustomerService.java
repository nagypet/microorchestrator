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

package hu.perit.microorchestrator.services.api;

import hu.perit.microorchestrator.services.model.CustomerDto;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;

import java.util.List;

public interface CustomerService
{
    CustomerDto getCustomerByUsername(String username) throws ResourceNotFoundException;

    CustomerDto getCustomerById(Long userId) throws ResourceNotFoundException;

    List<CustomerDto> getCustomers();
}

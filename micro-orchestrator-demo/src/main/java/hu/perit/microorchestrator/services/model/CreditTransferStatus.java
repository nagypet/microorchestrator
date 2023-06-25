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

package hu.perit.microorchestrator.services.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CreditTransferStatus
{
    REQUESTED(1L),
    EXECUTED(2L),
    FAILED(3L);

    private long code;
    private static Map<Long, CreditTransferStatus> mappings;

    private static synchronized Map<Long, CreditTransferStatus> getMappings()
    {
        if (mappings == null)
        {
            mappings = new HashMap<>();
        }

        return mappings;
    }

    CreditTransferStatus(Long code)
    {
        this.code = code;
        getMappings().put(code, this);
    }

    public static CreditTransferStatus fromCode(Long statusCode)
    {
        if (getMappings().containsKey(statusCode))
        {
            return getMappings().get(statusCode);
        }

        throw new IllegalStateException("Unexpected value: " + statusCode);
    }
}

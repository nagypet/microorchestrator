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

package hu.perit.microorchestrator.db.table;

import hu.perit.microorchestrator.services.model.CreditTransferStatus;
import hu.perit.microorchestrator.services.model.ForcedExceptionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = CreditTransferEntity.TABLE_NAME, schema = "DBO")
@ToString
public class CreditTransferEntity
{
    public static final String TABLE_NAME = "credit_transfer";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String debitorIban;

    @Column(nullable = false)
    private String creditorIban;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Long statusCode;

    @Column
    private ForcedExceptionType forcedExceptionForTesting;

    private String errorText;

    // For optimistic locking
    @Version
    @Column(nullable = false)
    private Long recVersion;

    public CreditTransferStatus getStatus()
    {
        if (this.statusCode == null)
        {
            return null;
        }

        return CreditTransferStatus.fromCode(this.statusCode);
    }

    public void setStatus(CreditTransferStatus status)
    {
        if (status == null)
        {
            this.statusCode = null;
            return;
        }

        this.statusCode = status.getCode();
    }
}

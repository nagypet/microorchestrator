package hu.perit.microorchestrator.services.model;

import lombok.Data;

import java.util.List;

@Data
public class CustomerDto
{
    private final String username;
    private final String name;
    private final Long id;
    private final List<String> accounts;
}

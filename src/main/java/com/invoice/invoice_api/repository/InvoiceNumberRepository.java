package com.invoice.invoice_api.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class InvoiceNumberRepository {

    private final EntityManager entityManager;

    public InvoiceNumberRepository(
            EntityManager entityManager
    ) {
        this.entityManager = entityManager;
    }

    public long getNextSequenceValue() {
        Number result =
                (Number) entityManager
                        .createNativeQuery(
                                """
                                SELECT nextval(
                                    'invoice_number_sequence'
                                )
                                """
                        )
                        .getSingleResult();

        return result.longValue();
    }
}

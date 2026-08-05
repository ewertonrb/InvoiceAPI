/*
 * ============================================================
 * INVOICE NUMBER SEQUENCE
 *
 * Provides a concurrency-safe sequential number for invoices.
 * The sequence is shared by all companies in the V1.
 * ============================================================
 */

CREATE SEQUENCE IF NOT EXISTS invoice_number_sequence
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;
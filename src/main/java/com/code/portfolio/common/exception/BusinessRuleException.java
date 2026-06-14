package com.code.portfolio.common.exception;

/** Lancada quando uma regra de negocio e violada. Mapeada para HTTP 422. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}

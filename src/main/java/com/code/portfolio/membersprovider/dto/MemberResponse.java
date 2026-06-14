package com.code.portfolio.membersprovider.dto;

/** Representacao de um membro exposta pela API externa (mockada). */
public record MemberResponse(Long id, String name, String attribution) {
}

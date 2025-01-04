package io.github.qifan777.knowledge.user.dto;

public record PasswordUpdateDTO(
    String oldPassword,
    String newPassword
) {} 
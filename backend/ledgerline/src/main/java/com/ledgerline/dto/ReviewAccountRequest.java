package com.ledgerline.dto;

import jakarta.validation.constraints.Size;

public record ReviewAccountRequest(

        @Size(max = 500, message = "Review note cannot exceed 500 characters")
        String reviewNote

) {
}
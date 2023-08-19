package com.onrender.themba.cashcardapi;

import org.springframework.data.annotation.Id;

public record CashCard(@Id Long id, Double amount) {
}

package com.youssef3b4afi.financetracker.transaction;

import com.youssef3b4afi.financetracker.category.Category;
import com.youssef3b4afi.financetracker.transaction.dto.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    /** Reads the category too, so it must be called inside a transaction or after a JOIN FETCH. */
    public TransactionResponse toResponse(Transaction transaction) {
        Category category = transaction.getCategory();
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getNote(),
                category.getId(),
                category.getName(),
                category.getType());
    }
}

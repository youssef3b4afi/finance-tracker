package com.youssef3b4afi.financetracker.transaction;

import com.youssef3b4afi.financetracker.category.Category;
import com.youssef3b4afi.financetracker.category.CategoryService;
import com.youssef3b4afi.financetracker.category.CategoryType;
import com.youssef3b4afi.financetracker.common.NotFoundException;
import com.youssef3b4afi.financetracker.transaction.dto.TransactionRequest;
import com.youssef3b4afi.financetracker.user.User;
import com.youssef3b4afi.financetracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit test: the repositories and CategoryService are Mockito fakes, so no Spring and no database. */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Long USER_ID = 1L;
    private static final User USER = new User("a@example.com", "hash");

    @Mock
    TransactionRepository transactions;

    @Mock
    CategoryService categoryService;

    @Mock
    UserRepository users;

    TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService(transactions, categoryService, users, new TransactionMapper());
    }

    @Test
    void createThrowsNotFoundAndSavesNothingWhenCategoryIsNotTheUsers() {
        when(categoryService.findOwned(USER_ID, 7L)).thenThrow(new NotFoundException("Category", 7L));

        assertThatThrownBy(() -> service.create(USER_ID, request(7L, "10.00")))
                .isInstanceOf(NotFoundException.class);
        verify(transactions, never()).save(any());
    }

    @Test
    void updateChangesFieldsAndTakesTypeFromNewCategory() {
        Category food = new Category(USER, "Food", CategoryType.EXPENSE);
        Category salary = new Category(USER, "Salary", CategoryType.INCOME);
        Transaction transaction = new Transaction(USER, food, new BigDecimal("10.00"), LocalDate.of(2026, 10, 1), null);
        when(transactions.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(transaction));
        when(categoryService.findOwned(USER_ID, 8L)).thenReturn(salary);

        var response = service.update(USER_ID, 5L, request(8L, "2500.00"));

        assertThat(response.amount()).isEqualByComparingTo("2500.00");
        assertThat(response.categoryName()).isEqualTo("Salary");
        assertThat(response.type()).isEqualTo(CategoryType.INCOME);
    }

    @Test
    void deleteThrowsNotFoundAndDeletesNothingWhenTransactionIsNotTheUsers() {
        when(transactions.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USER_ID, 5L)).isInstanceOf(NotFoundException.class);
        verify(transactions, never()).delete(any());
    }

    private static TransactionRequest request(Long categoryId, String amount) {
        return new TransactionRequest(categoryId, new BigDecimal(amount), LocalDate.of(2026, 10, 2), null);
    }
}

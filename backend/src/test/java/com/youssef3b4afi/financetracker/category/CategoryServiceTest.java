package com.youssef3b4afi.financetracker.category;

import com.youssef3b4afi.financetracker.category.dto.CategoryRenameRequest;
import com.youssef3b4afi.financetracker.category.dto.CategoryRequest;
import com.youssef3b4afi.financetracker.common.ConflictException;
import com.youssef3b4afi.financetracker.common.NotFoundException;
import com.youssef3b4afi.financetracker.transaction.TransactionRepository;
import com.youssef3b4afi.financetracker.user.User;
import com.youssef3b4afi.financetracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test: no Spring, no database. The repositories are Mockito fakes whose answers we script
 * with when(...).thenReturn(...), and we check what the service asked them with verify(...).
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    CategoryRepository categories;

    @Mock
    TransactionRepository transactions;

    @Mock
    UserRepository users;

    CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categories, transactions, users, new CategoryMapper());
    }

    @Test
    void createThrowsConflictAndSavesNothingWhenNameExists() {
        when(categories.existsByUserIdAndNameIgnoreCase(USER_ID, "Food")).thenReturn(true);

        assertThatThrownBy(() -> service.create(USER_ID, new CategoryRequest("Food", CategoryType.EXPENSE)))
                .isInstanceOf(ConflictException.class);
        verify(categories, never()).save(any());
    }

    @Test
    void renameToSameNameWithDifferentCaseSkipsDuplicateCheck() {
        Category food = new Category(new User("a@example.com", "hash"), "food", CategoryType.EXPENSE);
        when(categories.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(food));

        var response = service.rename(USER_ID, 5L, new CategoryRenameRequest("Food"));

        assertThat(response.name()).isEqualTo("Food");
        verify(categories, never()).existsByUserIdAndNameIgnoreCase(anyLong(), anyString());
    }

    @Test
    void renameThrowsNotFoundWhenCategoryIsNotTheUsers() {
        when(categories.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rename(USER_ID, 5L, new CategoryRenameRequest("Anything")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteThrowsNotFoundAndDeletesNothingWhenCategoryIsNotTheUsers() {
        when(categories.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USER_ID, 5L)).isInstanceOf(NotFoundException.class);
        verify(categories, never()).delete(any());
    }
}

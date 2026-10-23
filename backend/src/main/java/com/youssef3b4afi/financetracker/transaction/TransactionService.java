package com.youssef3b4afi.financetracker.transaction;

import com.youssef3b4afi.financetracker.category.Category;
import com.youssef3b4afi.financetracker.category.CategoryService;
import com.youssef3b4afi.financetracker.common.NotFoundException;
import com.youssef3b4afi.financetracker.common.PageResponse;
import com.youssef3b4afi.financetracker.transaction.dto.TransactionFilter;
import com.youssef3b4afi.financetracker.transaction.dto.TransactionRequest;
import com.youssef3b4afi.financetracker.transaction.dto.TransactionResponse;
import com.youssef3b4afi.financetracker.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactions;
    private final CategoryService categoryService;
    private final UserRepository users;
    private final TransactionMapper mapper;

    public TransactionService(TransactionRepository transactions, CategoryService categoryService,
                              UserRepository users, TransactionMapper mapper) {
        this.transactions = transactions;
        this.categoryService = categoryService;
        this.users = users;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> list(Long userId, TransactionFilter filter, int page, int size) {
        var result = transactions.search(userId, filter.from(), filter.to(), filter.categoryId(), filter.type(),
                PageRequest.of(page, size));
        return PageResponse.from(result.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(Long userId, Long id) {
        return mapper.toResponse(findOwned(userId, id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        // The category must be the user's own: categoryId comes from the request body, so it can't be trusted.
        Category category = categoryService.findOwned(userId, request.categoryId());
        Transaction transaction = new Transaction(users.getReferenceById(userId), category,
                request.amount(), request.date(), request.note());
        return mapper.toResponse(transactions.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = findOwned(userId, id);
        Category category = categoryService.findOwned(userId, request.categoryId());
        transaction.update(category, request.amount(), request.date(), request.note());
        return mapper.toResponse(transaction);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        transactions.delete(findOwned(userId, id));
    }

    private Transaction findOwned(Long userId, Long id) {
        return transactions.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Transaction", id));
    }
}

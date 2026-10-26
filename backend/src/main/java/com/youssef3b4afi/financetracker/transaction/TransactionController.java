package com.youssef3b4afi.financetracker.transaction;

import com.youssef3b4afi.financetracker.category.CategoryType;
import com.youssef3b4afi.financetracker.common.CurrentUser;
import com.youssef3b4afi.financetracker.common.PageResponse;
import com.youssef3b4afi.financetracker.transaction.dto.TransactionFilter;
import com.youssef3b4afi.financetracker.transaction.dto.TransactionRequest;
import com.youssef3b4afi.financetracker.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    /** e.g. GET /api/transactions?from=2026-10-01&to=2026-10-31&type=EXPENSE&page=0&size=20 */
    @GetMapping
    public PageResponse<TransactionResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Clamp instead of failing: page=-1 becomes 0, size=100000 becomes 100 (protects the server).
        var filter = new TransactionFilter(from, to, categoryId, type);
        return service.list(CurrentUser.id(jwt), filter, Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.get(CurrentUser.id(jwt), id);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody TransactionRequest request) {
        TransactionResponse created = service.create(CurrentUser.id(jwt), request);
        return ResponseEntity.created(URI.create("/api/transactions/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                      @Valid @RequestBody TransactionRequest request) {
        return service.update(CurrentUser.id(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(CurrentUser.id(jwt), id);
    }
}

package com.youssef3b4afi.financetracker.category;

import com.youssef3b4afi.financetracker.category.dto.CategoryRenameRequest;
import com.youssef3b4afi.financetracker.category.dto.CategoryRequest;
import com.youssef3b4afi.financetracker.category.dto.CategoryResponse;
import com.youssef3b4afi.financetracker.common.ConflictException;
import com.youssef3b4afi.financetracker.common.NotFoundException;
import com.youssef3b4afi.financetracker.user.User;
import com.youssef3b4afi.financetracker.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    /** Starter set every new user gets (US-4); they can rename or delete any of them afterwards. */
    private static final List<String> DEFAULT_INCOME = List.of("Salary", "Other Income");
    private static final List<String> DEFAULT_EXPENSE =
            List.of("Food", "Rent", "Transport", "Utilities", "Health", "Entertainment", "Shopping");

    private final CategoryRepository categories;
    private final UserRepository users;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository categories, UserRepository users, CategoryMapper mapper) {
        this.categories = categories;
        this.users = users;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long userId) {
        return categories.findAllByUserIdOrderByNameAsc(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long userId, Long id) {
        return mapper.toResponse(findOwned(userId, id));
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        requireUniqueName(userId, request.name());
        // getReferenceById doesn't query the users table; we only need the id for the foreign key.
        Category category = new Category(users.getReferenceById(userId), request.name(), request.type());
        return mapper.toResponse(categories.save(category));
    }

    /**
     * Called by AuthService.register. It joins the caller's transaction (the default "REQUIRED" propagation),
     * so if anything here fails, the new user is rolled back too: no account ever exists without its categories.
     */
    @Transactional
    public void createDefaults(User user) {
        List<Category> defaults = new ArrayList<>();
        DEFAULT_INCOME.forEach(name -> defaults.add(new Category(user, name, CategoryType.INCOME)));
        DEFAULT_EXPENSE.forEach(name -> defaults.add(new Category(user, name, CategoryType.EXPENSE)));
        categories.saveAll(defaults);
    }

    @Transactional
    public CategoryResponse rename(Long userId, Long id, CategoryRenameRequest request) {
        Category category = findOwned(userId, id);
        // Changing only the letter case ("food" -> "Food") is not a clash with itself.
        if (!category.getName().equalsIgnoreCase(request.name())) {
            requireUniqueName(userId, request.name());
        }
        category.rename(request.name());
        // No save() needed: the entity is "managed", so Hibernate writes the change when the transaction commits.
        return mapper.toResponse(category);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        // The "category still has transactions" rule (US-6) is added together with transactions.
        categories.delete(findOwned(userId, id));
    }

    /** Someone else's category gives the same 404 as a missing one, so ids can't be probed. */
    private Category findOwned(Long userId, Long id) {
        return categories.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Category", id));
    }

    private void requireUniqueName(Long userId, String name) {
        if (categories.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new ConflictException("Category already exists: " + name);
        }
    }
}

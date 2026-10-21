# Example slice: Category

Reference only — read when writing a new slice from scratch.

```java
// category/Category.java
@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;   // INCOME, EXPENSE

    protected Category() {}      // for JPA
    // constructor with required fields, getters, setters for mutable fields
}
```

```java
// category/CategoryRepository.java
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByUserIdOrderByNameAsc(Long userId);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
```

```java
// category/dto/CategoryRequest.java
public record CategoryRequest(
        @NotBlank @Size(max = 50) String name,
        @NotNull CategoryType type) {}

// category/dto/CategoryResponse.java
public record CategoryResponse(Long id, String name, CategoryType type) {}
```

```java
// category/CategoryMapper.java
@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType());
    }
}
```

```java
// category/CategoryService.java
@Service
public class CategoryService {
    private final CategoryRepository categories;
    private final TransactionRepository transactions;
    private final UserRepository users;
    private final CategoryMapper mapper;
    // constructor injection (no @Autowired fields)

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(Long userId) {
        return categories.findAllByUserIdOrderByNameAsc(userId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest req) {
        if (categories.existsByUserIdAndNameIgnoreCase(userId, req.name()))
            throw new ConflictException("Category already exists: " + req.name());
        var c = new Category(users.getReferenceById(userId), req.name(), req.type());
        return mapper.toResponse(categories.save(c));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        var c = categories.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Category", id));
        if (transactions.existsByCategoryId(id))
            throw new ConflictException("Category still has transactions");
        categories.delete(c);
    }
}
```

```java
// category/CategoryController.java
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;

    @GetMapping
    public List<CategoryResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(CurrentUser.id(jwt));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                   @Valid @RequestBody CategoryRequest req) {
        var created = service.create(CurrentUser.id(jwt), req);
        return ResponseEntity.created(URI.create("/api/categories/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(CurrentUser.id(jwt), id);
    }
}
```

```java
// common/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}
```

---
name: add-api-endpoint
description: Step-by-step guide for adding a new REST endpoint to the OBS Spring Boot backend. Follows the established layered architecture. Use whenever adding a new controller method or resource.
---

# Add a New API Endpoint (Backend)

Follow the established layered architecture: `Controller → Service → Repository`.

## Checklist

- [ ] DTO created (request and/or response)
- [ ] Service interface method declared
- [ ] ServiceImpl method implemented with `@Transactional`
- [ ] Controller method added
- [ ] Tests written (service unit test + controller test)
- [ ] Committed

## Step-by-Step

### 1. Create the DTO

In `src/main/java/com/obs/backend/dto/`:

```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MyEntityResponse {
    private Long id;
    private String name;

    public static MyEntityResponse from(MyEntity entity) {
        return MyEntityResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
```

### 2. Declare the Service Interface

In `src/main/java/com/obs/backend/service/MyEntityService.java`:

```java
public interface MyEntityService {
    List<MyEntityResponse> getAll();
    MyEntityResponse getById(Long id);
}
```

### 3. Implement the Service

In `src/main/java/com/obs/backend/service/impl/MyEntityServiceImpl.java`:

```java
@Service
@Transactional
public class MyEntityServiceImpl implements MyEntityService {

    private final MyEntityRepository repository;

    public MyEntityServiceImpl(MyEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyEntityResponse> getAll() {
        return repository.findAll().stream()
                .map(MyEntityResponse::from)
                .toList();
    }
}
```

### 4. Add the Controller

In `src/main/java/com/obs/backend/controller/MyEntityController.java`:

```java
@RestController
@RequestMapping("/api/my-entities")
public class MyEntityController {

    private final MyEntityService myEntityService;

    public MyEntityController(MyEntityService myEntityService) {
        this.myEntityService = myEntityService;
    }

    @GetMapping
    public ResponseEntity<List<MyEntityResponse>> getAll() {
        return ResponseEntity.ok(myEntityService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MyEntityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(myEntityService.getById(id));
    }

    @PostMapping
    public ResponseEntity<MyEntityResponse> create(@Valid @RequestBody MyEntityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(myEntityService.create(request));
    }
}
```

### 5. Write Tests

**Service test** (`src/test/...service/MyEntityServiceTest.java`):
```java
@ExtendWith(MockitoExtension.class)
class MyEntityServiceTest {
    @Mock MyEntityRepository repository;
    @InjectMocks MyEntityServiceImpl service;

    @Test
    void getAll_returnsListOfResponses() { ... }
}
```

**Controller test** (`src/test/...controller/MyEntityControllerTest.java`):
```java
@WebMvcTest(MyEntityController.class)
class MyEntityControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean MyEntityService service;

    @Test
    void getAll_returns200() throws Exception {
        mockMvc.perform(get("/api/my-entities"))
               .andExpect(status().isOk());
    }
}
```

### 6. Run Tests

```bash
cd obs-backend && mvn test
```

All 18 existing tests + new tests must pass.

### 7. Commit

```bash
git add src/main/java/com/obs/backend/dto/MyEntityResponse.java
git add src/main/java/com/obs/backend/service/MyEntityService.java
git add src/main/java/com/obs/backend/service/impl/MyEntityServiceImpl.java
git add src/main/java/com/obs/backend/controller/MyEntityController.java
git add src/test/...
git commit -m "feat: add GET /api/my-entities endpoint"
```

## HTTP Status Code Reference

| Situation | Status |
|-----------|--------|
| Successful GET / PUT | 200 |
| Successful POST (created) | 201 |
| Successful DELETE | 204 |
| Validation error | 400 |
| Entity not found | 404 |

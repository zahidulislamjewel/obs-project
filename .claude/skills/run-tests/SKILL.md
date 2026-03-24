---
name: run-tests
description: Run the full test suite for the OBS project. Runs backend Maven tests (18 tests using H2 in-memory DB). Reports pass/fail clearly.
---

# Run OBS Tests

Run all backend tests and report results.

## Backend Tests

```bash
cd obs-backend
mvn test
```

Expected: **18 tests, 0 failures, 0 errors** — `BUILD SUCCESS`

Test classes:
- `BookServiceTest` — 10 tests (service layer, Mockito)
- `BookControllerTest` — 8 tests (HTTP layer, MockMvc)

Tests use H2 in-memory database. No running PostgreSQL required.

## Report Format

After running, report:

```
Backend tests: X/18 passing
Status: PASS / FAIL

[If FAIL — show failing test names and error messages]
```

If any tests fail, do not proceed with merge or PR creation. Fix the failures first.

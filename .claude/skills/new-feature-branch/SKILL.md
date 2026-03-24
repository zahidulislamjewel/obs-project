---
name: new-feature-branch
description: Create a properly named feature branch for OBS work. Enforces branch naming conventions for backend, frontend, and integration work.
---

# Create OBS Feature Branch

Follow branch naming conventions before starting any new work.

## Branch Naming Convention

| Work type | Pattern | Example |
|-----------|---------|---------|
| Backend feature | `dev/backend/OBS-<N>` | `dev/backend/OBS-5` |
| Frontend feature | `dev/frontend/OBS-<N>` | `dev/frontend/OBS-6` |
| Integration | `integration/OBS-INT-<N>` | `integration/OBS-INT-2` |
| Bug fix | `fix/<area>/OBS-<N>` | `fix/backend/OBS-7` |

## Steps

1. Ensure you are on `main` and it is up to date:
```bash
git checkout main
git pull origin main
```

2. Create and checkout the new branch:
```bash
git checkout -b <branch-name>
```

3. Confirm:
```bash
git branch --show-current
```

## Commit Message Convention

```
feat: <description>      # new functionality
fix: <description>       # bug fix
test: <description>      # tests only
chore: <description>     # config, tooling, dependencies
refactor: <description>  # code restructure, no behaviour change
docs: <description>      # documentation only
```

Keep commits small and atomic. One logical change per commit.

## Before Opening a PR

- [ ] All backend tests pass: `cd obs-backend && mvn test`
- [ ] No unintended files staged (`target/`, `node_modules/`, `.angular/`, `dist/`)
- [ ] Commits follow naming convention
- [ ] Branch is up to date with `main`

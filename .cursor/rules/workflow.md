# Feature Development Workflow

Before implementing a feature:

1. Understand existing architecture.
2. Search for similar implementations.
3. Reuse existing patterns.

When creating API functionality:

1. Create DTOs.
2. Create Retrofit service methods.
3. Create Repository interface.
4. Create Repository implementation.
5. Create UseCase.
6. Create ViewModel logic.
7. Connect UI.

When modifying existing code:

- Prefer extending existing solutions.
- Avoid rewriting working code.
- Explain large refactors before performing them.
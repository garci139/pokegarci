# Architecture Rules

This project follows MVVM and Clean Architecture.

Layers:

Presentation
- Activities
- Fragments
- ViewModels
- UI State

Domain
- UseCases
- Repository Interfaces

Data
- Repository Implementations
- Retrofit Services
- DTOs
- Local Storage

Rules:

- ViewModels must never call Retrofit directly.
- ViewModels must never contain business logic.
- Business logic belongs in UseCases.
- Repositories are accessed through interfaces.
- UI must only communicate with ViewModels.
- Domain layer must not depend on Android framework classes.
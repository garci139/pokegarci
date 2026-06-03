# Android Development Rules

UI

- Follow Material Design guidelines.
- Reuse existing components.
- Avoid hardcoded strings.
- Use string resources.
- Use dimension resources when appropriate.

Lifecycle

- Respect Activity and Fragment lifecycle.
- Avoid memory leaks.
- Use viewLifecycleOwner in Fragments.

Coroutines

- Launch UI coroutines from ViewModel.
- Use viewModelScope.
- Avoid launching coroutines from Activities when business logic is involved.

Performance

- Avoid unnecessary recompositions.
- Avoid nested RecyclerViews unless required.
- Minimize allocations inside loops.
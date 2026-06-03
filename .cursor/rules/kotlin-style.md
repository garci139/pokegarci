# Kotlin Coding Standards

Use:
- data classes
- sealed classes
- StateFlow
- Coroutines

Avoid:
- GlobalScope
- Mutable public properties
- Unnecessary nullable types

Prefer:

val over var

expression bodies when readability improves

when over multiple if statements

Use Result<T> for operations that can fail.

Always:

- Remove unused imports.
- Remove dead code.
- Use immutable collections whenever possible.
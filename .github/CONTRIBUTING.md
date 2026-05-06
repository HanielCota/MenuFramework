# Contributing

## Development

Use Java 21 and the included Gradle wrapper.

```powershell
.\gradlew.bat test spotbugsMain
```

On Unix-like systems:

```bash
./gradlew test spotbugsMain
```

## Code Style

- Keep changes focused.
- Prefer early returns.
- Do not introduce `else` branches.
- Keep public APIs annotated with JSpecify nullness annotations.
- Do not use Lombok.
- Use `ItemTemplate` instead of raw `ItemStack` in framework APIs.

## Pull Requests

Include a concise summary, validation steps, and any compatibility notes.


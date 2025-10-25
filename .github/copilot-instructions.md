# Copilot Instructions for Nitrite Database

## Project Overview

Nitrite Database is an open source embedded NoSQL database for Java. It's a multi-module Maven project that supports both in-memory and file-based persistent storage.

**Key Features:**
- Embedded, serverless document store
- Document-oriented with schema-less collections
- Extensible storage engines (MVStore, RocksDB)
- Full-text search and indexing
- Transaction support
- Android compatibility (API Level 24+)

## Repository Structure

This is a multi-module Maven project with the following modules:

- `nitrite` - Core database module
- `nitrite-bom` - Bill of materials for dependency management
- `nitrite-jackson-mapper` - Jackson-based JSON mapper
- `nitrite-mvstore-adapter` - MVStore storage adapter
- `nitrite-rocksdb-adapter` - RocksDB storage adapter
- `nitrite-spatial` - Spatial indexing support
- `nitrite-support` - Support utilities
- `nitrite-native-tests` - GraalVM native image tests
- `potassium-nitrite` - Kotlin extension

## Development Environment

### Java Versions
- **Minimum supported:** Java 8
- **Primary testing:** Java 11 and 17
- **Native image testing:** Java 17 and 21 with GraalVM

### Build Tool
Maven is the build tool. Key commands:

```bash
# Build the project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests only
mvn test

# Build specific module
mvn -pl nitrite clean install
```

## Code Guidelines

### Java Standards
1. **Minimum compatibility:** Code must compile and run on Java 8
2. **Code style:** Follow existing code formatting in the repository (see `.editorconfig`)
3. **Dependencies:** Check `pom.xml` files before adding new dependencies
4. **API changes:** Be cautious with public API changes - this is a library used by others

### Testing Requirements
1. **Always run tests:** Execute `mvn test` before submitting changes
2. **Write tests:** New features should include unit tests
3. **Test coverage:** Maintain or improve code coverage (tracked via CodeCov)
4. **Cross-platform:** Consider Linux, macOS, and Windows compatibility

### Module-Specific Guidelines
- When modifying storage adapters (`nitrite-mvstore-adapter`, `nitrite-rocksdb-adapter`), ensure compatibility with the core module
- Changes to `nitrite` core module may require updates to adapter modules
- Kotlin extension (`potassium-nitrite`) should mirror Java API capabilities

## Pull Request Best Practices

### Before Creating a PR
1. Run full build: `mvn clean install`
2. Ensure all tests pass across Java 11 and 17
3. Check for compilation warnings
4. Update documentation if changing public APIs
5. Add or update tests for your changes

### PR Scope
- Keep PRs focused on a single issue or feature
- Include issue references in PR description
- Document breaking changes clearly
- Update CHANGELOG.md for significant changes

### Review Process
- All PRs require review before merging
- CI must pass (build on Linux, macOS, Windows)
- CodeQL security analysis must pass
- Code coverage should not decrease significantly

## Security Considerations

1. **No secrets in code:** Never commit API keys, passwords, or credentials
2. **Input validation:** Validate all user inputs, especially in query operations
3. **File operations:** Be careful with file I/O operations in storage adapters
4. **Dependencies:** Check for known vulnerabilities in dependencies
5. **CodeQL:** Address any CodeQL security findings

## Common Tasks

### Good Tasks for Copilot
- Bug fixes in specific modules
- Adding unit tests to improve coverage
- Documentation updates (JavaDoc, README)
- Code style and formatting improvements
- Refactoring isolated methods/classes
- Adding new query operators or filters
- Implementing feature requests with clear specifications

### Tasks Requiring Human Review
- Breaking API changes
- Changes to core database engine logic
- Modifications to transaction handling
- Storage format changes
- Security-sensitive operations
- Multi-module architectural changes

## Documentation

- **JavaDoc:** All public APIs must have JavaDoc comments
- **README updates:** Update README.md if changing features or usage
- **User guide:** Major features may need documentation at https://nitrite.dizitart.com
- **Code comments:** Add comments for complex logic, not obvious code

## Gradle vs Maven
This project uses **Maven**, not Gradle. All build commands should use `mvn`.

## Related Projects

- **Potassium Nitrite:** Kotlin extension within this repository
- **Nitrite Flutter:** Separate repository for Flutter/Dart version
- **Deprecated:** Nitrite DataGate and Nitrite Explorer (no longer maintained)

## Communication

- **Issues:** Use GitHub issues for bugs and feature requests
- **Discussions:** Use GitHub Discussions for questions and community interaction
- **Contributing:** See CONTRIBUTING.md for detailed contribution guidelines

## Additional Resources

- **Website:** https://nitrite.dizitart.com
- **Documentation:** https://nitrite.dizitart.com/java-sdk/getting-started/index.html
- **API Docs:** https://javadoc.io/doc/org.dizitart/nitrite
- **GitHub:** https://github.com/nitrite/nitrite-java

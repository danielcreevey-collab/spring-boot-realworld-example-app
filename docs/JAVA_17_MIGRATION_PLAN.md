# Java 11 to Java 17 Migration Plan

## Executive Summary

This document outlines the comprehensive migration plan for upgrading the Spring Boot RealWorld Example Application from Java 11 to Java 17. The migration is relatively straightforward as Spring Boot 2.6.3 officially supports Java 17, and the current dependency versions are largely compatible.

## Current State Analysis

### Java Version Configuration

The following files currently specify Java 11:

| File | Location | Current Value |
|------|----------|---------------|
| `build.gradle` | Lines 10-11 | `sourceCompatibility = '11'`, `targetCompatibility = '11'` |
| `.github/workflows/gradle.yml` | Lines 20-24 | `java-version: '11'` |
| `README.md` | Line 47 | "You'll need Java 11 installed." |

### Current Dependency Versions

| Dependency | Current Version | Java 17 Compatible |
|------------|-----------------|---------------------|
| Spring Boot | 2.6.3 | Yes |
| Gradle Wrapper | 7.4 | Yes (requires 7.3+) |
| Lombok | 1.18.22 | Yes |
| Flyway | 8.0.5 | Yes |
| MyBatis Spring Boot | 2.2.2 | Yes |
| Netflix DGS | 4.9.21 | Yes |
| JJWT | 0.11.2 | Yes |
| RestAssured | 4.5.1 (Groovy 3.0.9) | Yes |
| Joda-Time | 2.10.13 | Yes |
| SQLite JDBC | 3.36.0.3 | Yes |

### Compatibility Assessment

**Good News:** The current configuration is well-positioned for Java 17 migration:

1. **Gradle 7.4** is already installed, which fully supports Java 17 (minimum required is 7.3)
2. **Spring Boot 2.6.3** was designed and tested with Java 17 support
3. **Lombok 1.18.22** supports Java 17
4. **Flyway 8.0.5** supports Java 17
5. **Groovy 3.0.9** (via RestAssured) supports Java 17
6. **No internal JDK API usage** was found in the codebase (`sun.*`, `jdk.internal.*`, `com.sun.*`)

## Migration Steps

### Phase 1: Configuration Updates

#### Step 1.1: Update build.gradle

Update the Java version settings in `build.gradle`:

```gradle
// Change from:
sourceCompatibility = '11'
targetCompatibility = '11'

// To:
sourceCompatibility = '17'
targetCompatibility = '17'
```

**File:** `build.gradle` (Lines 10-11)

#### Step 1.2: Update GitHub Actions Workflow

Update the CI/CD pipeline in `.github/workflows/gradle.yml`:

```yaml
# Change from:
- name: Set up JDK 11
  uses: actions/setup-java@v2
  with:
    distribution: zulu
    java-version: '11'

# To:
- name: Set up JDK 17
  uses: actions/setup-java@v2
  with:
    distribution: zulu
    java-version: '17'
```

**File:** `.github/workflows/gradle.yml` (Lines 20-24)

#### Step 1.3: Update README.md

Update the prerequisites documentation:

```markdown
# Change from:
You'll need Java 11 installed.

# To:
You'll need Java 17 installed.
```

**File:** `README.md` (Line 47)

### Phase 2: Verification and Testing

#### Step 2.1: Local Build Verification

Run the following commands to verify the build works with Java 17:

```bash
# Clean any previous build artifacts
./gradlew clean

# Compile the project
./gradlew compileJava

# Run all tests
./gradlew test
```

#### Step 2.2: Check for Reflection Warnings

During the test run, monitor the console output for any warnings related to:
- `InaccessibleObjectException`
- `illegal reflective access`
- `--add-opens` suggestions

If such warnings appear, they may indicate libraries that need updating or JVM arguments that need to be added.

#### Step 2.3: Run the Application Locally

```bash
./gradlew bootRun
```

Verify the application starts successfully and test key endpoints:
- `GET http://localhost:8080/tags` - Should return available tags
- `POST http://localhost:8080/users` - Test user registration
- `POST http://localhost:8080/users/login` - Test authentication

#### Step 2.4: GraphQL Endpoint Verification

Test the GraphQL endpoint at `http://localhost:8080/graphql` to ensure the Netflix DGS framework works correctly with Java 17.

### Phase 3: CI/CD Verification

#### Step 3.1: Push Changes and Monitor CI

After making the configuration changes:

1. Commit all changes
2. Push to a feature branch
3. Create a Pull Request
4. Monitor the GitHub Actions workflow for any failures

#### Step 3.2: Address CI Failures

If CI fails, common issues and solutions include:

| Issue | Solution |
|-------|----------|
| Compilation errors | Check for deprecated API usage removed in Java 17 |
| Test failures | Check for reflection-related issues in test libraries |
| Spotless failures | Update Spotless plugin if needed |

## Potential Breaking Changes: Java 11 to Java 17

### Removed APIs and Features

The following APIs were removed between Java 11 and Java 17 that could potentially affect the application:

1. **Nashorn JavaScript Engine** (removed in Java 15) - Not used in this project
2. **RMI Activation** (removed in Java 17) - Not used in this project
3. **Applet API** (deprecated for removal) - Not used in this project

### Stronger Encapsulation

Java 17 enforces stronger encapsulation of JDK internals:

- The `--illegal-access` option no longer works (removed in Java 17)
- Access to internal APIs now throws `InaccessibleObjectException` by default

**Impact on this project:** Low - No direct usage of internal JDK APIs was found in the codebase.

### Security Changes

- TLS 1.0 and 1.1 are disabled by default
- Some older cipher suites are disabled

**Impact on this project:** Low - The application uses standard HTTPS/TLS configurations.

### Language Changes

New language features available in Java 17 (optional to adopt):

- **Records** (Java 14+) - Can simplify DTOs
- **Sealed Classes** (Java 17) - Can improve domain modeling
- **Pattern Matching for instanceof** (Java 16) - Cleaner type checks
- **Text Blocks** (Java 15) - Multi-line strings

These are optional enhancements and not required for the migration.

## Optional Dependency Updates

While not strictly required for Java 17 compatibility, the following updates are recommended for improved stability and security:

### Recommended Updates

| Dependency | Current | Recommended | Reason |
|------------|---------|-------------|--------|
| Spring Boot | 2.6.3 | 2.6.15 (latest 2.6.x) | Security patches and bug fixes |
| JJWT | 0.11.2 | 0.11.5 | Security patches |
| RestAssured | 4.5.1 | 4.5.1 | Already compatible |
| SQLite JDBC | 3.36.0.3 | 3.42.0.0+ | Performance improvements |

### Future Consideration: Spring Boot 3.x

Spring Boot 3.x requires Java 17 as minimum and uses Jakarta EE 9+ namespaces. This is a separate, larger migration that should be planned after the Java 17 upgrade is stable. Key changes for Boot 3.x include:

- `javax.*` to `jakarta.*` namespace migration
- Spring Framework 6.x
- Updated minimum versions for many dependencies

## Rollback Plan

If critical issues are discovered after migration:

1. Revert the three configuration files to Java 11 settings
2. Ensure CI/CD uses Java 11
3. Document any issues discovered for future migration attempts

## Testing Checklist

Before considering the migration complete, verify:

- [ ] `./gradlew clean build` succeeds
- [ ] All unit tests pass (`./gradlew test`)
- [ ] Application starts successfully (`./gradlew bootRun`)
- [ ] REST API endpoints respond correctly
- [ ] GraphQL endpoint responds correctly
- [ ] JWT authentication works
- [ ] Database operations work (CRUD on articles, users, comments)
- [ ] CI/CD pipeline passes
- [ ] No reflection warnings in logs

## Timeline Estimate

| Phase | Estimated Duration |
|-------|-------------------|
| Phase 1: Configuration Updates | 15 minutes |
| Phase 2: Local Verification | 30-60 minutes |
| Phase 3: CI/CD Verification | 15-30 minutes |
| **Total** | **1-2 hours** |

## Conclusion

The migration from Java 11 to Java 17 for this Spring Boot application is low-risk due to:

1. Spring Boot 2.6.3's official Java 17 support
2. Compatible Gradle version (7.4)
3. No usage of deprecated or removed JDK APIs
4. All major dependencies already support Java 17

The migration primarily involves updating three configuration files and verifying the application works correctly through testing.

## References

- [Spring Boot 2.6 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.6-Release-Notes)
- [Java 17 Migration Guide](https://docs.oracle.com/en/java/javase/17/migrate/getting-started.html)
- [Gradle Java 17 Support](https://docs.gradle.org/7.3/release-notes.html)
- [Netflix DGS Documentation](https://netflix.github.io/dgs/)

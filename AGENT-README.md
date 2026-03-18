# AGENT-README: carbon-kernel

> Optimized for AI agent consumption. Dense, factual, structured.

---

## Quick Reference (Key Facts for Common Tasks)

| Fact | Value |
|------|-------|
| Group ID | `org.wso2.carbon` |
| Artifact ID | `carbon-kernel` |
| Version | `5.3.3-SNAPSHOT` |
| Java source/target | **1.8** (set in root pom.xml — key for Java 21 migration) |
| OSGi runtime | Eclipse Equinox 3.14.0 |
| Build tool | Maven 3.0+ multi-module |
| CI JDK | AdoptOpenJDK 11 (ubuntu-latest, GitHub Actions) |
| Full build | `mvn clean install` |
| Build (no tests) | `mvn clean install -DskipTests` |
| Entry point class | `org.wso2.carbon.launcher.Main` |
| Root pom modules | launcher, core, archetypes, tools, pax-exam-container-carbon, features, distribution, tests |

---

## 1. Project Identity

- **Name:** WSO2 Carbon Kernel
- **Coordinates:** `org.wso2.carbon:carbon-kernel:5.3.3-SNAPSHOT`
- **Java target:** 1.8 (both `maven.compiler.source` and `maven.compiler.target` in root `pom.xml`)
- **OSGi runtime:** Eclipse Equinox 3.14.0
- **Framework:** OSGi R6 (Core spec 6.0.0)
- **Build:** Maven multi-module, parent POM at `parent/pom.xml`
- **Purpose:** Foundational OSGi-based server framework for WSO2 products

---

## 2. Module Map

| Directory | Artifact ID | Packaging | Purpose |
|-----------|-------------|-----------|---------|
| `parent/` | `carbon-kernel-parent` | pom | Dependency & plugin version management |
| `launcher/` | `org.wso2.carbon.launcher` | jar | Main entry point; creates and starts OSGi framework via FrameworkFactory SPI |
| `core/` | `org.wso2.carbon.core` | bundle | Core framework: runtime lifecycle, CarbonContext, startup resolver, JMX |
| `archetypes/` | `org.wso2.carbon.archetypes` | pom | Maven archetypes/templates for bundle and component development |
| `tools/` | `org.wso2.carbon.tools` | pom | Standalone developer utility tools |
| `pax-exam-container-carbon/` | `pax-exam-container-carbon` | pom | Custom Pax Exam OSGi integration test container |
| `features/` | `carbon-kernel-features` | pom | OSGi P2 feature definitions for Eclipse update sites |
| `distribution/` | `wso2carbon-kernel` | pom | Assembles final product ZIP artifact |
| `tests/` | `carbon-kernel-tests` | pom | All test suites and test artifacts |

---

## 3. Architecture: Key Abstractions

### Launcher Layer (`launcher/`)
- **`Main`** — JVM entry point; parses args, creates `CarbonServer`, calls `start()`
- **`CarbonServer`** — creates OSGi `Framework` via `FrameworkFactory` SPI; loads initial bundles from `carbon.home/lib`; manages framework lifecycle

### Core Layer (`core/`)
- **`CarbonRuntime`** (interface) — server-level abstraction; provides `CarbonConfiguration` and manages server state
- **`Runtime`** (interface) — pluggable runtime contract; lifecycle methods: `init()`, `start()`, `stop()`, `beginMaintenance()`, `endMaintenance()`
- **`RuntimeService`** (interface) — OSGi service for registering and managing `Runtime` instances
- **`CarbonContext`** — thread-local state per request (read-only view)
- **`PrivilegedCarbonContext`** — mutable thread-local state (privileged operations)
- **`StartupOrderResolver`** — manifest-driven startup ordering; reads `Carbon-Component` and `Provide-Capability` OSGi headers; resolves dependency order before activating components
- **`RequiredCapabilityListener`** — callback interface; notified when all required OSGi services/capabilities become available
- **`CarbonCoreComponent`** — DS `@Component` that bootstraps the core; registers `CarbonRuntime` as OSGi service
- **`DataHolder`** — singleton holding `BundleContext` and other framework-level references

---

## 4. Key Source File Paths

```
launcher/src/main/java/org/wso2/carbon/launcher/
  Main.java                          # JVM entry point
  CarbonServer.java                  # OSGi framework creator and lifecycle manager

core/src/main/java/org/wso2/carbon/kernel/
  internal/
    CarbonCoreComponent.java         # DS component bootstrapping the core
    DataHolder.java                  # Singleton for BundleContext and global refs
    startupresolver/
      StartupOrderResolver.java      # Manifest-driven startup ordering
  runtime/
    Runtime.java                     # Pluggable runtime interface
    RuntimeService.java              # Runtime management OSGi service interface
  context/
    CarbonContext.java               # Thread-local read-only context
    PrivilegedCarbonContext.java     # Thread-local mutable context
  config/model/
    CarbonConfiguration.java         # Configuration model (server name, ports, etc.)
```

---

## 5. Build & Test Commands

```bash
# Full build with all tests
mvn clean install

# Build without tests (faster)
mvn clean install -DskipTests

# Unit tests only
mvn test

# Integration tests (auto-activated when tests not skipped)
mvn clean install

# CI build (update snapshots, batch mode)
mvn clean install -U -B

# Build specific module
mvn clean install -pl core -am
```

**CI:** GitHub Actions at `.github/workflows/pr-builder.yml`
- JDK: AdoptOpenJDK 11
- OS: ubuntu-latest
- Command: `mvn clean install -U -B`

---

## 6. Testing Infrastructure

| Component | Library | Version |
|-----------|---------|---------|
| Test framework | TestNG | 6.9.4 |
| Mocking | EasyMock | 3.4 |
| Mocking (static/final) | PowerMock | 1.6.5 |
| OSGi integration tests | Pax Exam | 4.9.1 |
| OSGi container | Custom Carbon Pax Exam container | (local module) |
| Coverage | JaCoCo | 0.8.2 |
| Static analysis | SpotBugs | (configured in parent pom) |

**Test suite locations:**
- Unit tests: `core/src/test/resources/testng.xml`
- OSGi integration tests: `tests/osgi-tests/src/test/resources/testng.xml`
- SpotBugs exclusions: `spotbugs-exclude.xml` (repo root)

---

## 7. OSGi Patterns

- **Declarative Services:** Components use `@Component` and `@Reference` annotations (bnd/DS tooling)
- **Startup ordering:** Manifest headers `Carbon-Component` and `Provide-Capability` drive `StartupOrderResolver`
- **Service availability:** `RequiredCapabilityListener` callback when all required OSGi services are registered
- **BundleContext:** Injected into `DataHolder` singleton during DS component activation
- **Bundle packaging:** `maven-bundle-plugin` generates `MANIFEST.MF` with OSGi headers
- **Logging:** Pax Logging (SLF4J API + Log4j2 backend); all code uses SLF4J `LoggerFactory`

---

## 8. Key Dependencies (managed in `parent/pom.xml`)

| Dependency | Version |
|-----------|---------|
| OSGi Core | 6.0.0 |
| Eclipse Equinox (org.eclipse.osgi) | 3.14.0 |
| Pax Logging API | 2.2.2-wso2v1 |
| Pax Logging Log4j2 | 2.2.2-wso2v1 |
| Pax Exam | 4.9.1 |
| TestNG | 6.9.4 |
| EasyMock | 3.4 |
| PowerMock | 1.6.5 |
| JaCoCo Maven Plugin | 0.8.2 |
| JAXB API (Java 11 compat) | added explicitly |
| Activation API (Java 11 compat) | added explicitly |

---

## 9. Important Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` (root) | Aggregate POM; sets Java 1.8 source/target; declares module build order |
| `parent/pom.xml` | All dependency versions, plugin management, SpotBugs config |
| `spotbugs-exclude.xml` | SpotBugs filter — known exclusions for static analysis |
| `.github/workflows/pr-builder.yml` | CI pipeline — AdoptJDK 11, ubuntu, `mvn clean install -U -B` |
| `distribution/` | Product ZIP assembly descriptor |
| `core/src/main/resources/` | Core bundle resources (OSGI-INF, config schemas) |

---

## 10. Java Version Migration Notes (for Java 21 task)

Current state (as of v5.3.3-SNAPSHOT):
- `maven.compiler.source=1.8`, `maven.compiler.target=1.8` in root `pom.xml`
- CI uses JDK 11 but compiles to Java 8 bytecode
- JAXB and Activation APIs already added as explicit dependencies (Java 9+ compatibility groundwork)
- No `module-info.java` files — not yet modularized
- Uses `sun.*` internal APIs in some tools (verify before migration)
- PowerMock 1.6.5 may have Java 17+ compatibility issues (requires investigation)
- Pax Logging and Equinox versions should be verified for Java 21 support

Migration checklist hint:
1. Update `maven.compiler.source` and `maven.compiler.target` to `21`
2. Update CI JDK version in `.github/workflows/pr-builder.yml`
3. Check for removed/encapsulated APIs (`--add-opens` requirements)
4. Verify OSGi/Equinox version supports Java 21
5. Update PowerMock or replace with Mockito (Java 21 compatible)

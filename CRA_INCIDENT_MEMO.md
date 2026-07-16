# Supply-Chain Audit & CRA Incident Memo — GameFlix

**Prepared for:** Module 5 assignment (Step 8 — Supply-Chain Audit & CRA)
**Scope:** `gameflix-0.0.1-SNAPSHOT.jar` (the packaged Spring Boot application, all bundled dependencies)
**Tools used:** [Syft](https://github.com/anchore/syft) (SBOM generation) and [Trivy](https://github.com/aquasecurity/trivy) (vulnerability scanning against the SBOM)
**Date of audit:** 2026-07-16

---

## 1. Why this matters: the EU Cyber Resilience Act (CRA)

The Cyber Resilience Act imposes obligations on anyone who places a "product
with digital elements" on the market — which includes a web application like
GameFlix if it ships to users. Two obligations are directly relevant here:

- **Article 13 — SBOM as a due-diligence artifact.** A manufacturer must be
  able to identify and document the components in their product, including
  third-party and open-source dependencies, so that vulnerabilities in those
  components can be found and tracked over the product's lifetime.
- **Article 14 — vulnerability handling and incident reporting.** Once a
  manufacturer becomes aware of an actively exploited vulnerability or a
  severe incident affecting the product's security, it must notify ENISA
  (via the relevant CSIRT) on a strict timeline:
  - **24 hours** — early warning that a vulnerability/incident exists.
  - **72 hours** — a fuller notification with an initial assessment and,
    where possible, mitigating measures.
  - **14 days** — a final report with a root-cause analysis and details of
    the corrective action taken.

This memo documents the SBOM/scan audit performed on GameFlix and then walks
through what a CRA-compliant response would look like if the most severe
finding below were discovered in a fielded, production instance of the app.

---

## 2. Methodology

1. Built the application: `mvn -B package -DskipTests` → `target/gameflix-0.0.1-SNAPSHOT.jar`.
2. Generated a CycloneDX SBOM directly from the built jar:
   `syft target/gameflix-0.0.1-SNAPSHOT.jar -o cyclonedx-json=sbom.cdx.json`
   This catalogs every embedded dependency (55 components — Spring Framework,
   Spring Boot, Hibernate, Jackson, Tomcat, MySQL connector, jjwt, logging
   libraries, etc.) with exact versions, not just the direct dependencies
   declared in `pom.xml`.
3. Scanned the SBOM for known vulnerabilities:
   `trivy sbom sbom.cdx.json --format json --output trivy-report.json`
   (also saved as a human-readable table in `trivy-report.txt`).
4. Reviewed the findings, remediated what could be safely fixed, rebuilt, and
   re-scanned to measure the before/after delta.

Artifacts committed alongside this memo: `sbom.cdx.json`, `trivy-report.json`,
`trivy-report.txt`.

---

## 3. Findings

### Before remediation (Spring Boot parent `3.3.2`, as originally submitted)

| Severity | Count |
|----------|-------|
| CRITICAL | 4 |
| HIGH     | 21 |
| MEDIUM   | 18 |
| LOW      | 12 |
| **Total** | **55** |

All 4 CRITICAL findings were in `tomcat-embed-core` (the embedded servlet
container Spring Boot pulls in transitively) — none were in code GameFlix
authored directly. This is the central lesson of a supply-chain audit: the
project's own ~1,500 lines of code had zero reported vulnerabilities, but the
55 open-source components it depends on carried real, dated exposure that
`pom.xml` alone doesn't reveal at a glance.

### Remediation taken

`spring-boot-starter-parent` was pinned at `3.3.2` (July 2024). Spring Boot's
parent BOM manages the versions of every transitive dependency (Tomcat,
Jackson, Spring Framework, Spring Security, etc.), so bumping the parent to
the latest patch release in the same `3.3.x` line — **`3.3.13`** — pulls in
patched versions of those libraries with no API changes and no risk of
breaking the app. This was applied to `pom.xml`, and the full test suite
(JWT + UserService unit tests) was re-run to confirm no regression before
re-scanning.

### After remediation (Spring Boot parent `3.3.13`)

| Severity | Count | Change |
|----------|-------|--------|
| CRITICAL | 3 | −1 |
| HIGH     | 14 | −7 |
| MEDIUM   | 9  | −9 |
| LOW      | 8  | −4 |
| **Total** | **34** | **−21** |

### Residual risk (not fixed by this patch bump)

The remaining findings — 3 CRITICAL, all still in `tomcat-embed-core` — are
fixed upstream only in Tomcat `10.1.55`/`11.0.22`, which Spring Boot doesn't
ship until the `3.5.x`/`4.0.x` line. Similarly, two HIGH `jackson-databind`
findings and one HIGH `spring-core` finding require a Spring Boot `3.4+`/
Spring Framework `6.2.11` upgrade. Those are **minor/major version jumps**,
not patch releases — they can introduce breaking API and configuration
changes, so they were deliberately **not** applied as part of this audit.
They are logged here as accepted residual risk with a recommended follow-up:
plan and test a Spring Boot 3.5.x upgrade in a separate, dedicated change
rather than bundling it into a supply-chain patch pass.

---

## 4. Exploitability analysis: is any remaining CRITICAL/HIGH finding actually reachable?

A CVE present in the dependency tree isn't automatically a live risk — it
depends on whether GameFlix's code actually exercises the vulnerable feature.
Each remaining CRITICAL and the two remaining HIGH `jackson-databind`
findings were checked against the app's actual configuration and source
(`application.properties` plus a full `src/` search for the relevant
feature), not just their CVSS score.

| Finding | Requires | GameFlix's actual configuration | Reachable? |
|---------|----------|----------------------------------|------------|
| **CVE-2026-41293** (CRITICAL — HTTP/2 request header validation bypass) | HTTP/2 enabled on the connector | No `server.http2.enabled`, no SSL/connector customization anywhere in `application.properties` or `src/` — the app serves plain HTTP/1.1 via Spring Boot's default embedded Tomcat | **No** |
| **CVE-2026-43512** (CRITICAL — digest-authentication bypass) | Tomcat's built-in Realm + digest `<login-config>` | No `web.xml`, no Realm configuration anywhere; all authentication is the custom `JwtAuthFilter` (`OncePerRequestFilter`), entirely outside Tomcat's container-managed security | **No** |
| **CVE-2026-43515** (CRITICAL — improper authorization via conflicting method/extension `<security-constraint>`s) | Declarative `<security-constraint>` entries in `web.xml` | GameFlix has no `web.xml` at all — it's a pure Spring Boot app; the one protected endpoint (`/api/me`) is gated by a `FilterRegistrationBean`-scoped servlet filter, not a declarative security constraint | **No** |
| **CVE-2026-54512 / CVE-2026-54513** (HIGH — Jackson arbitrary code execution via polymorphic type handling) | `ObjectMapper.activateDefaultTyping(...)` or an equivalent explicit polymorphic-deserialization config | No `ObjectMapper` bean, no `@JsonTypeInfo`, no `PolymorphicTypeValidator`, and no Jackson settings in `application.properties` anywhere in the codebase — the app relies entirely on Spring Boot's default (safe) Jackson autoconfiguration | **No** |

**Conclusion:** none of the remaining CRITICAL or HIGH findings are exploitable
through any code path GameFlix currently exercises. This is **not** the same
finding as "no critical vulnerabilities" — the vulnerable code ships inside
the jar regardless of whether it's reachable today, and a future change
(enabling HTTP/2 for performance, adding a `web.xml`, someone reintroducing
Tomcat-managed auth, or a future feature that turns on Jackson default
typing) could make any of these live again without anyone realizing an
already-known CVE had reopened. That's precisely the gap a recurring
vulnerability-handling process — not a single one-time audit — is meant to
close; see Section 6.

---

## 5. Simulated CRA incident: `CVE-2026-43512`

To exercise the CRA reporting workflow, treat the most severe unresolved
finding as if it were discovered actively exploited against a live GameFlix
deployment:

**Finding:** `CVE-2026-43512` — Apache Tomcat authentication bypass via
digest authentication, present in `tomcat-embed-core` (bundled by Spring Boot,
current installed version `10.1.42`; fixed in `9.0.118` / `10.1.55` /
`11.0.22`). Severity: **CRITICAL**.

**Simulated timeline a CRA-covered manufacturer would follow:**

| Deadline | Action |
|----------|--------|
| **T+24h** | Early warning filed with the national CSIRT/ENISA: identify the product (GameFlix), the vulnerable component (`tomcat-embed-core` via `spring-boot-starter-web`), and that active exploitation is suspected. No root cause or fix required yet — the point is speed, not completeness. |
| **T+72h** | Follow-up notification: confirm exploitation vector (digest-auth bypass reachable via any endpoint using Tomcat's built-in digest authentication — GameFlix does not currently use this feature, which narrows the practical impact), give a severity/impact assessment, and note interim mitigation (e.g., disable/avoid digest auth entirely; restrict network exposure) while a durable fix is prepared. |
| **T+14 days** | Final report: root cause (unpatched transitive dependency shipped in a Spring Boot parent version predating the Tomcat fix), the corrective action taken (upgrade path to a Spring Boot line bundling Tomcat ≥ 10.1.55, or an explicit `tomcat.version` property override as an interim step), and confirmation via a re-run SBOM/vulnerability scan that the CVE no longer appears in the build. |

**Interim mitigation available today without a major upgrade:** Spring Boot
lets you override a managed dependency's version directly in `pom.xml`
properties (e.g. `<tomcat.version>10.1.55</tomcat.version>`) without bumping
the whole parent BOM. This is a reasonable "T+72h" stop-gap; it wasn't applied
here so this audit could show the actual, unmodified result of a same-line
patch bump alone.

---

## 6. Why this belongs in the pipeline, not just this one memo

A one-time scan only proves the dependency tree was clean on 2026-07-16. New
CVEs are disclosed against already-shipped code constantly (several findings
above carry 2026 CVE IDs against libraries GameFlix has used unmodified since
2024). The durable fix is to run `syft`/`trivy` on every build — most
naturally as an added step in the existing `.github/workflows/deploy.yml` CI
job, failing the build (or at least flagging it) on new CRITICAL findings —
so vulnerability discovery happens continuously rather than only when someone
remembers to run an audit by hand.

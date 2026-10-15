# Configuration Externalization Pattern

**Category:** Operations
**One-liner:** Move configuration out of application code and deployment packages into a centralized store that services fetch at **runtime** (while they are running) — so changing config doesn't require rebuilding and **redeploying** (releasing a new build to the servers).

---

## Part 0 — Concepts you need first

### What is "configuration"?
Values that control how an application behaves but **aren't its logic**:
- Database **connection strings** (address and options for connecting) and **credentials** (usernames, passwords)
- **API keys** (secret tokens identifying your program to another service) and **endpoint** URLs of external services
- **Timeout** durations, retry counts, **thread pool** sizes
- Feature flags — switches turning functionality on and off
- Region-specific values: currency, tax rate, language

**The test:** would you want a different value in development vs production, or one region vs another, without changing code? Then it's configuration.

### What does "hardcoded" mean?
Written directly into source:

```java
String dbUrl = "jdbc:postgresql://prod-db-01:5432/orders";
```

Changing it means editing source, **recompiling** and redeploying; the value is **frozen into the built artifact**.

### What is a "deployment package"?
The bundle you ship — a **JAR** (packaged Java program), a **container image** (an app plus everything it needs, used to start containers), a zip. **Bundled configuration** travels *inside* it: better than hardcoding (no recompile), but **still frozen at build time** — changing it means rebuilding and redeploying.

### What is a "deployment cycle"?
Build → test → review → deploy → verify: maybe an hour in a well-run organisation, days in a regulated one. **The chapter's core complaint:** that entire process, for changing one number.

### What is an environment?
A separate copy of the system for a purpose — **development**, **staging/testing** (rehearsal), **production** (live users). Same code, different configuration: databases, API keys, scale.

### What is a secret?
Configuration that must not be disclosed — database passwords, API keys, **encryption keys**. **Secrets are configuration with much stricter handling requirements** — a recurring theme below.

---

## Part 1 — The framing

Would an architect carve every building's blueprint into its walls, or keep blueprints separate, accessible and modifiable? The same applies to software. In **microservices**, **each service has configuration governing its behaviour** — network settings, database parameters, feature toggles — traditionally hardcoded or bundled. **So what happens when you need to change one parameter? A whole deployment cycle for a minor tweak?**

---

## Part 2 — The problem

### Volume and diversity
Many services, possibly in **different languages and technology stacks**, each with its own conventions for where config lives and how it's read — managing it all quickly becomes a nightmare.

### The drawbacks of hardcoding
Hardcoding ties configuration **tightly to code**:
- **Any change, however minor, needs a new application version and a full redeployment**
- It **slows the adaptability** of your services
- **It introduces risk**: a config error in production means **another build-and-deploy round** — wasted resources and, at worst, **significant downtime**

> A typo in a database **hostname** is a five-second fix conceptually, but a full deployment cycle in practice — done while production is broken.

### The burden of scale
In a **monolith** configuration is usually **centralized in one place**; in microservices each service maintains its own. That **amplifies the workload** and **the risk of inconsistencies and errors** — imagine changing the blueprint of every building in a city.

> You **rotate** a database password (replace it, as routine security hygiene) and update fourteen services. You miss one. It keeps working until its **connection pool** (already-open, reused connections) recycles — possibly hours later — then fails in a way that looks unrelated to anything you did.

### Inflexibility of deployments
With hardcoded or bundled config, **each minor change may force a full redeployment**, which **negates independent deployment** — microservices' main selling point — and breeds **configuration inconsistencies across environments** (dev, test, prod).

### Inability to adapt to dynamic environments
Optimal configuration shifts with conditions: **network settings** with **traffic patterns**, **feature toggles** with **user behaviour**. Traditional config makes **real-time** response nearly impossible — services are **stuck with outdated configuration until the next deployment**.

### Security implications
Hardcoding risks **exposing or leaking sensitive information** — database credentials, API keys.

> The usual way: a credential hardcoded in source is **committed** to **version control** (e.g. **Git**, which records every change, who made it, and when) and now lives in the **repository**'s history **forever**, readable by everyone with access. Rewriting Git history is painful and rarely complete; **rotating the credential** is the only real remedy.

---

## Part 3 — The solution

Apply **separation of concerns** to configuration: **externalize it from application code** into a **separate, centralized location** (the blueprints kept in a central town hall). Configuration is no longer in the deployment package; it lives in a **configuration server** (a service whose only job is to hand out configuration) or a network-accessible file, and the service **retrieves it at runtime**, always getting current values.

### Why this is advantageous
- **One unified location** — solves volume and diversity: **whatever a service's language or stack, configuration comes from the same place**.
- **Support for dynamic environments** — fetched at runtime, so it **updates in real time**; no waiting for the next deployment.
- **Preserved independent deployment** — config changes **no longer need a redeploy**; they're updated centrally and available immediately.
- **Enhanced security** — secrets are **no longer scattered across services**; central storage makes **security policies and controls easier to enforce**.
- **Efficient scalability** — a new service's configuration just joins the central store; no touring every service.
- **Consistency across environments** — all environments' configuration is **centrally managed**, reducing errors when promoting dev → test → prod.

---

## Part 4 — Architecture

Two components: **the microservice** and the **configuration server** (or file).

### The interaction
The service, **at startup or at pre-defined intervals**, asks the server for its configuration — **connection settings, API keys, any environment-specific data** — and the server returns what that service needs. This happens **dynamically, at runtime**, so the service adapts **without a restart or redeployment**, reducing downtime and increasing **agility**.

```
   ┌────────────────────────┐
   │  Configuration Server  │◀──── backed by Git repo / store
   │  organised by:         │
   │   · environment        │
   │   · microservice       │
   │   · instance           │
   └───────────┬────────────┘
       fetch at startup /
       on interval / on refresh
     ┌─────────┼─────────┐
     ▼         ▼         ▼
  Service A  Service B  Service C
```

### The configuration server
The nerve centre: it stores, retrieves and manages everyone's configuration, organised **by environment** (dev, staging, prod), **by microservice**, and **by instance** (one running copy). That organisation is what fixes traditional config management — it's not just centralized, it's *navigable*.

### Dynamic adaptability in practice
Database connection parameters suddenly change: **instead of redeploying**, you update the config server, and the service **receives the new parameters on its next fetch and carries on seamlessly.**

### Security — safeguarding sensitive configuration
Not every "book" is open to everyone. The server protects sensitive values through **encryption** and **access controls** (who may read or change what); centralization makes **strict policies easier to enforce**, reducing accidental exposure and attack risk.

### Scalability and consistency
New services just add their configuration to the server. And you **switch configuration as you move through environments** — no environment-specific code, no mixed-up configurations.

---

## Part 5 — Worked example: Spring Cloud Config

**Spring Cloud Config** (part of Spring Cloud, the microservices toolkit on the Spring Java framework) is widely adopted in Java for centralized configuration, for its simplicity.

### The configuration server

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServer {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}
```

The `@EnableConfigServer` **annotation** turns an application into a configuration server in a few lines.

### The configuration store — a Git repository

Tell the server where configuration lives, in a **properties file** (plain `key=value` settings):

```properties
spring.cloud.config.server.git.uri=https://github.com/yourusername/config-repo
```

> **Using Git is genuinely clever.** A version-controlled config store gives you, free: full history of every change and its author, **diffs** of exactly what changed, **pull requests** (proposed changes reviewed before merging) and **code review** *for configuration*, and **rollback to any previous state** with a standard Git operation. Configuration becomes a first-class engineering artifact, not a value someone typed into a form.

### The client setup
Add the Spring Cloud Config Client **dependency** to the service's **build file** (`pom.xml` or `build.gradle`) and annotate the main class with `@EnableConfigClient` — it now consumes configuration from the server.

### Pulling configuration — the `@Value` annotation

```java
@Value("${my.property}")
private String myProperty;
```

`@Value` fetches `my.property` from the store into the field. **The code knows only a property name** — not a location or mechanism.

### Refreshing without restarting

```
POST /actuator/refresh
```

A **POST** to this endpoint, provided by **Spring Boot Actuator** (Spring's management module), makes the client **re-fetch configuration** and apply it **without restarting**. The whole pattern in one endpoint: change centrally, refresh, done.

### Encryption and decryption

```properties
encrypt.key=MySuperSecretKey
```

Sets the key for encrypting/decrypting properties, via the `/encrypt` and `/decrypt` endpoints.

> ⚠️ **Notice the irony.** `encrypt.key` is itself a secret, sitting in a plain properties file — you've encrypted every secret and left the key in the open. In production the master key belongs in a dedicated **secrets manager** (built to store, issue and rotate secrets) — **HashiCorp Vault, AWS Secrets Manager, or a cloud KMS** (key management service, holding keys in protected hardware) — fetched by the config server at startup. **The pattern relocates the secret problem; it doesn't eliminate it.** For secrets, a purpose-built manager usually beats a general config server.

---

## Part 6 — Considerations and implications

### Data security
The pattern aids security but **adds a new attack surface**: sensitive data now stored **remotely**. How secure is the store, and the transfer? Beyond the `MySuperSecretKey` example: **secure, encrypted connections** and **regularly rotated keys**.

### The configuration server as a single point of failure

Centralizing **introduces a single point of failure**:

> **If the config server goes down, every microservice is paralysed — unable to fetch or refresh configuration.**

**Mitigations:**
- **Redundancy** — **multiple instances** in a **high-availability configuration**, with **regular health checks**.
- **Client-side caching of last-known-good configuration** — each service keeps the last values it fetched successfully. This is what makes the trade acceptable: you swapped "config changes need a redeploy" for "a config outage could take everything down", and caching turns the second from fatal to inconvenient because **running services keep their current values**. It's the same trick, explained in full, as in [Service Discovery Part 7.3](../05-service-discovery/README.md#3-fault-tolerance-and-high-availability).

### Configuration versioning and rollbacks
Faster rollouts raise the question: **can you revert quickly when something goes wrong?** Storing configuration in **Git** means you can **roll back to a previous state** — but only with **good practices**: documented changes, correctly labelled versions. Git provides the capability; discipline makes it usable at 3am during an **incident**.

### Service discovery and the config service
Services come and go with **load** — **how does a new service find the config service?** **Hardcoding its address isn't scalable** — and is ironic: hardcoding a location to avoid hardcoding configuration. Use **service discovery** — **Netflix's Eureka** or **HashiCorp's Consul** find services dynamically, **including the configuration server** *([Service Discovery](../05-service-discovery/README.md))*.

### Performance implications
**Remote fetching adds latency**, especially under server load or network issues. **Caching helps** — Spring Cloud Config clients **cache remote properties by default** — but raises its own questions: **how often to refresh?** (too often hammers the server; too rarely delays changes) and **what memory cost?** A balance between **performance and freshness**.

---

## Part 7 — Real-world use cases

| Case | Situation | How the pattern helps |
|---|---|---|
| **E-commerce platform** | Inventory, Order, User Management — **hundreds of services across production, staging and development**, each with databases and APIs | All configuration on a central server; **changes made in one place, reflected across the system** |
| **Financial services firm** | Highly regulated; **API keys and database passwords must not be exposed** | Sensitive config stored centrally, **possibly encrypted**, fetched **over a secure (encrypted) channel**; **nothing sensitive in the codebase or containers** — what **auditors** want to see |
| **Global fast-food chain** | Outlets in many countries, each with **its own tax laws, currency and language** | At boot each app **fetches its country's configuration** — per-country behaviour from one shared codebase |
| **Tech startup** | Streaming service, **agile** (short cycles, small frequent releases) with **multiple deployments a day**, each needing **feature flags or A/B testing parameters** | Services fetch updated config when needed — **faster, smoother deployments** |
| **Telecommunications giant** | Many services managing **bandwidth, routing information and security protocols** | All network configuration central; each service fetches what it needs |

---

## Part 8 — Common beginner misconceptions

- **"Externalizing config means putting it in a file next to the app."** That's still bundled. Externalized means **outside the deployment artifact**, retrieved over the network.
- **"A config server is the right place for secrets."** Better than hardcoding, but a dedicated secrets manager (Vault, AWS Secrets Manager) adds **leasing** (secrets issued for a limited time, then expiring), automatic rotation, fine-grained access policies and **audit logs** that a config server lacks.
- **"Environment variables are the same thing."** An **environment variable** (a named value the OS passes a program at start) is a good, simple form of externalization and often sufficient — but gives **no central management, versioning, or refresh without restart**.
- **"If the config server is down, everything stops."** Only without client-side caching; with it **running services keep running** — you just can't start new ones or push changes.
- **"Config changes are low risk because there's no deploy."** The opposite: no deploy means **no build, no tests, no review** unless you add them — hence Git-backed stores with required pull requests.
- **"One config server for everything."** Separate by **trust boundary** (parts of the system that shouldn't automatically trust each other); not every service should read every other's config.

---

## Part 9 — Check your understanding

1. What's the difference between hardcoded, bundled, and externalized configuration?
2. Why does hardcoded configuration undermine the main benefit of microservices?
3. What does backing the config store with Git give you for free?
4. What's the central irony of the `encrypt.key` property?
5. Why do you need service discovery in order to do configuration externalization well?
6. What makes the config server's single-point-of-failure risk acceptable?

*(Answers: 1 — hardcoded is in source and needs recompiling; bundled is in the deployment package and
needs rebuilding and redeploying; externalized lives outside the artifact and is fetched at runtime;
2 — independent deployability, because a one-line config change forces a full redeploy of the service;
3 — change history, authorship, diffs, code review, and rollback to any previous state; 4 — it's a
secret protecting all other secrets, stored in **plaintext** (unencrypted), so it belongs in a real secrets manager;
5 — hardcoding the config server's address in every service would reintroduce the exact problem you're
solving; 6 — client-side caching of last-known-good values, so running services survive a config
server outage.)*

---

## Part 10 — Interview soundbite (30 seconds)

> "Configuration externalization moves config out of code and deployment packages into a central config
> server that services fetch at runtime. Hardcoded config ties every change to a full build-and-deploy
> cycle, destroying the independent deployability that's the point of microservices, and stops you
> reacting to conditions until the next release; it also scatters secrets across services and into
> version-control history. With a config server you organise config by environment, service and
> instance; services fetch at startup or on an interval, and Spring Cloud Config's actuator refresh
> endpoint applies changes without a restart. It's typically backed by Git — the clever part — giving
> versioning, diffs, code review and rollback for free. Trade-offs: the config server is a single point
> of failure, so it needs HA plus client-side caching of last-known-good values; it's a new attack
> surface holding your secrets, so encryption in transit and key rotation matter; and services need
> service discovery to find it, since hardcoding its address reintroduces the problem. For secrets I'd
> use a dedicated manager like Vault — leasing, rotation and audit logging."

---

## Part 11 — Implementation

### A. Runnable demo

Full source: **[`demos/ConfigExternalizationDemo.java`](../../../demos/chapters/13-config-externalization/ConfigExternalizationDemo.java)**

```bash
cd demos && ./gradlew runCh13
```

Layered resolution — later layers override earlier ones:

```java
Map<String, String> fetch() {
    Map<String, String> merged = new LinkedHashMap<>();
    layers.values().forEach(merged::putAll);   // default <- environment <- instance
    return merged;
}
```

The cache that survives a server outage:

```java
void refresh() {
    try {
        cache = server.fetch();
        cachedVersion = server.version();
    } catch (IllegalStateException e) {
        // server unreachable - KEEP the previous config rather than failing
    }
}
```

And what makes runtime flips work — read config **at use time**, not at startup:

```java
String checkout() {
    return config.flagEnabled("feature.newCheckout")   // read on every call
            ? "NEW checkout flow" : "legacy checkout flow";
}
```

### B. What you should see

```
=== 3. Change a value + refresh, NO restart ===
  before refresh: ... timeout=1000ms ...
  after refresh:  ... timeout=5000ms ...
  >> same running process, new value

=== 4. Layering: instance overrides environment overrides default ===
  ... timeout=9000ms ...
  >> instance layer won (9000), overriding environment (5000)

=== 5. Feature flag flipped centrally ===
  checkout now: legacy checkout flow
  checkout now: NEW checkout flow
  and svc2 too: NEW checkout flow
  >> every service flipped at once, with no deployment

=== 6. Config server outage ===
    [client] server DOWN - keeping cached config (v8)
  checkout still: NEW checkout flow
  >> running services keep working on cached config
```

**Section 5 is the payoff** — one central change flipped every running service at once, with no build or deploy. **Section 6 is the safety net** that makes centralizing acceptable.

### C. Production — Spring Cloud Config

**The server** (same `@EnableConfigServer` class as Part 5, here named `ConfigServerApplication`), configured in YAML:

```yaml
server.port: 8888
spring:
  cloud.config.server.git:
    uri: https://github.com/acme/config-repo
    default-label: main
    clone-on-start: true
    search-paths: '{application}'          # per-service folders
  security.user:
    name: configuser
    password: ${CONFIG_SERVER_PASSWORD}    # itself injected, not committed
```

**The repository layout** — this gives you environment layering:
```
config-repo/
├── application.yml                 # defaults for EVERY service
├── application-prod.yml            # defaults for every service in prod
├── order-service.yml               # all environments of one service
├── order-service-dev.yml
└── order-service-prod.yml          # most specific: wins
```

**The client:**
```yaml
spring:
  application.name: order-service          # picks which files apply
  config.import: "optional:configserver:http://config-server:8888"
  cloud.config:
    fail-fast: true                        # don't start with wrong config
    retry: { initial-interval: 1000, max-attempts: 6, multiplier: 1.5 }
```

**Refresh without restart** — the crucial bit is `@RefreshScope` (a **bean** is an object Spring creates and manages; `curl` sends HTTP requests from the command line):
```java
@RefreshScope                              // bean is recreated on refresh
@Component
public class CheckoutService {

    @Value("${feature.newCheckout:false}")
    private boolean newCheckoutEnabled;

    public String checkout() {
        return newCheckoutEnabled ? newFlow() : legacyFlow();
    }
}
```
```bash
curl -X POST http://order-service:8080/actuator/refresh
```

For **fleet**-wide refresh (every running instance), **Spring Cloud Bus** broadcasts over **Kafka** or **RabbitMQ** (message brokers) so one call refreshes all:
```bash
curl -X POST http://any-instance:8080/actuator/busrefresh
```

**Secrets belong in a secrets manager, not here** (`KUBERNETES` auth means Vault trusts the identity Kubernetes gives each **pod**):
```yaml
spring.cloud.vault:
  uri: https://vault:8200
  authentication: KUBERNETES            # pod identity, no static credentials
  kv: { enabled: true, backend: secret }
```
Vault gives **leasing, automatic rotation and audit logs**, none of which a general config server provides. Ordinary configuration in Git; secrets in Vault.

**Kubernetes-native alternative** — often all you need. A **ConfigMap** is Kubernetes' built-in configuration object, which can be **mounted as a volume** (exposed to the container as files):
```yaml
apiVersion: v1
kind: ConfigMap
metadata: { name: order-service-config }
data:
  http.timeoutMs: "3000"
  feature.newCheckout: "true"
---
# Mounted as a volume, changes propagate to the file within ~1 min
volumes:
  - name: config
    configMap: { name: order-service-config }
```
ConfigMaps mounted as **volumes** update live; consumed as **environment variables** they do **not** — the pod must restart. That trips people up constantly.

### D. Implementation pitfalls

**1. Reading config once into a `static final` field** (set once, shared by the class). Refresh then does nothing — the value was captured at **class-load time**. Use `@RefreshScope`, or read through a provider at each use.

**2. No client-side caching, or `fail-fast` misconfigured.** Without caching, a config server outage takes down every service. With `fail-fast: true` **and** retry, startup waits and retries rather than booting with silently wrong defaults.

**3. Secrets in the Git config repo.** Once committed, they're in history permanently. Use Vault or **sealed-secrets** (encrypted in Git, decryptable only by the cluster), and treat any leaked credential as needing rotation, not deletion.

**4. The encryption key stored beside the encrypted values.** Part 5's irony — locking the door and leaving the key in it. The master key belongs in a KMS or Vault, injected at startup.

**5. No validation before config propagates.** A typo in a Git commit reaches production in seconds with no build or test to catch it. Add a **CI** (continuous integration — automated checks on every change) job validating the repo, and require pull requests — config's speed is a hazard as well as a feature.

**6. Changing config in production directly.** If the repo is the **source of truth**, an **out-of-band** change (made outside the normal process) is silently reverted by the next refresh — a baffling incident.

**7. A single config server instance.** A single point of failure for the whole estate. Run at least two **replicas** behind a service, and monitor them.

**8. Hardcoding the config server's address.** Self-defeating. Use service discovery or a stable Kubernetes Service **DNS** name.

**9. Assuming env-var ConfigMaps refresh.** They don't; only volume mounts propagate without a restart.

---

## Related patterns

- **[Service Discovery](../05-service-discovery/README.md)** — how services locate the config server; identical HA and caching concerns
- **[Sidecar](../09-sidecar/README.md)** — a sidecar can fetch config and expose it locally to the application
- **[Strangler Fig](../02-strangler-fig/README.md)** — the feature flags driving migration routing live in externalized config
- **[Circuit Breaker](../06-circuit-breaker/README.md)** — breaker thresholds are configuration you'll want to tune without redeploying

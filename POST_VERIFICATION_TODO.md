# After `tech.pyrx` Namespace Is Verified on Maven Central

## 1. Re-run the publish workflow

```bash
gh run rerun 25243932378 --repo PYRX-Tech/pyrx-synapse-java
```

Or retag if the run has expired:

```bash
cd pyrx-synapse-java
git tag -d v0.1.0 && git push origin :refs/tags/v0.1.0
git tag v0.1.0 && git push origin v0.1.0
```

## 2. Verify it's published

Check https://central.sonatype.com/artifact/tech.pyrx/synapse — it may take 10-30 minutes to appear after a successful deploy.

## 3. Update Java examples to use Maven Central

Change both `pyrx-synapse-java-example/pom.xml` and `pyrx-synapse-spring-example/pom.xml` from local system path dependency to:

```xml
<dependency>
    <groupId>tech.pyrx</groupId>
    <artifactId>synapse</artifactId>
    <version>0.1.0</version>
</dependency>
```

Remove the `<scope>system</scope>` and `<systemPath>` entries.

## 4. Push updated examples

```bash
cd pyrx-synapse-java-example && git add -A && git commit -m "Use published Maven Central dependency" && git push
cd ../pyrx-synapse-spring-example && git add -A && git commit -m "Use published Maven Central dependency" && git push
```

## 5. Make Java SDK repo public

```bash
gh repo edit PYRX-Tech/pyrx-synapse-java --visibility public --accept-visibility-change-consequences
```

## 6. Update marketing site developers docs

Add Java SDK page at `/developers/sdks/java` with Maven/Gradle install instructions, update SDKs index and sidebar.

## 7. Add Java/Spring code tabs to home page

Update the "Integrate in minutes" section in `sdk-section.tsx` if not already done.

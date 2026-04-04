# Build, Run, and Validate

## Primary commands

### Build
```bash
./gradlew build
```
Runs formatting before checks.

### Compile only
```bash
./gradlew compileKotlinMacosArm64
```

### Format
```bash
./gradlew ktlintFormat
```

### Build native binary
```bash
./gradlew macosArm64Binaries
```
Native output:
`build/bin/macosArm64/releaseExecutable/spendex.kexe`

### Run without installing
```bash
./gradlew run --args="accounts list"
```

### Install globally
```bash
./install.sh
```
Installs `spndx` to `/usr/local/bin`.

## Validation guidance

- Prefer `./gradlew build` for full validation.
- Use the compile task for faster iteration when full validation is unnecessary.
- Do not treat running the built binary as the main verification step.
- Rebuild `code-index.json` after declaration or structure changes.

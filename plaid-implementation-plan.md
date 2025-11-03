# Plaid Authorization Implementation Plan

## Overview
Implement real OAuth authorization flow with Chase bank via Plaid API, following the existing architectural patterns in the codebase.

## Current State Analysis

### Existing Components
- **DAO Layer**: `AccountDao`, `TokenDao` with JSON file persistence to `~/.spndx/`
- **Service Layer**: `PlaidService` interface with mock implementation
- **Interpreter Layer**: `AccountCommandInterpreter` orchestrates business logic
- **Models**: Plaid models defined (`PlaidLinkTokenResponse`, `PlaidAccessTokenResponse`, etc.)
- **Dependencies**: Ktor client (darwin), Arrow-KT for Either, kotlinx.serialization

### Missing Components
1. Configuration DAO to read `~/.spndx/app-data-sandbox.json`
2. Real PlaidService implementation with HTTP client
3. OAuth redirect server (temporary local HTTP server)
4. Browser launcher (platform-specific)
5. Integration of full OAuth flow in the interpreter

## Implementation Plan

### Phase 1: Configuration Management

**Component**: `ConfigDao` and implementation

**Files to Create**:
- `src/commonMain/kotlin/config/PlaidConfig.kt` - Data model
- `src/commonMain/kotlin/config/ConfigDao.kt` - Interface
- `src/commonMain/kotlin/config/JsonConfigDao.kt` - expect class declaration
- `src/macosArm64Main/kotlin/config/JsonConfigDao.kt` - actual implementation

**Details**:
```kotlin
@Serializable
data class PlaidConfig(
    val client_id: String,
    val secret: String,
    val redirect_url: String
)

interface ConfigDao {
    suspend fun loadPlaidConfig(): Either<String, PlaidConfig>
}
```

**Implementation Strategy**:
- Use existing `FileSystemHelper` for file reading
- Read from `${HOME}/.spndx/app-data-sandbox.json`
- Parse with kotlinx.serialization
- Return `Either<String, PlaidConfig>` for error handling

---

### Phase 2: HTTP Client Setup

**Component**: Shared Ktor HttpClient

**Files to Create**:
- `src/commonMain/kotlin/plaid/HttpClientFactory.kt`

**Details**:
- Create singleton HttpClient with:
  - ContentNegotiation plugin (kotlinx.serialization JSON)
  - Timeout configuration (connect: 30s, request: 60s)
  - Darwin engine (macOS)

**Configuration**:
- Base URL: `https://sandbox.plaid.com` (sandbox environment)
- Content-Type: `application/json`

---

### Phase 3: Real Plaid Service Implementation

**Component**: `PlaidServiceImpl` replacing mock

**Files to Modify**:
- `src/commonMain/kotlin/plaid/PlaidModels.kt` - Add request models
- `src/commonMain/kotlin/plaid/PlaidServiceImpl.kt` - Create new file

**Enums for Products and Countries**:
```kotlin
enum class PlaidProduct {
    @SerialName("auth") AUTH,
    @SerialName("transactions") TRANSACTIONS,
    @SerialName("investments") INVESTMENTS,
    @SerialName("liabilities") LIABILITIES
}

enum class PlaidCountryCode {
    @SerialName("US") US,
    @SerialName("CA") CA,
    @SerialName("GB") GB
}
```

**Request Models Needed**:
```kotlin
@Serializable
data class LinkTokenCreateRequest(
    val client_id: String,
    val secret: String,
    val client_name: String,
    val user: LinkTokenUser,
    val products: List<PlaidProduct>,
    val country_codes: List<PlaidCountryCode>,
    val language: String,
    val redirect_uri: String
)

@Serializable
data class LinkTokenUser(
    val client_user_id: String
)

@Serializable
data class PublicTokenExchangeRequest(
    val client_id: String,
    val secret: String,
    val public_token: String
)
```

**Endpoints to Implement**:

1. **createLinkToken**: POST `/link/token/create`
   - Input: username, config (NO Bank parameter - users select institution in Plaid Link UI)
   - Request body: LinkTokenCreateRequest with:
     - products: [PlaidProduct.AUTH, PlaidProduct.TRANSACTIONS]
     - country_codes: [PlaidCountryCode.US]
     - language: "en"
     - client_name: "Spendex"
     - user.client_user_id: username
   - Output: `Either<String, String>` (link token)

2. **exchangePublicToken**: POST `/item/public_token/exchange`
   - Input: public token, config
   - Request body: PublicTokenExchangeRequest
   - Output: `Either<String, PlaidAccessTokenResponse>`

3. **getAccounts**: POST `/accounts/get`
   - Keep or implement as needed
   - Input: access token, config
   - Output: `Either<String, PlaidAccountsResponse>`

**Error Handling**:
- Use `bind()` or `flatMap()` on Either - NO NESTING
- All errors handled at the end of the call chain
- Simply print error message for now (future: centralized error handler)
- Use `either { }` builder from Arrow

---

### Phase 4: OAuth Redirect Server

**Component**: Temporary HTTP server to receive OAuth callback

**Files to Create**:
- `src/commonMain/kotlin/plaid/OAuthRedirectServer.kt` - expect class
- `src/macosArm64Main/kotlin/plaid/OAuthRedirectServer.kt` - actual implementation

**Dependencies to Add**:
- `ktor-server-core`
- `ktor-server-cio` (or netty for macOS)

**Design**:
```kotlin
interface OAuthRedirectServer : AutoCloseable {
    suspend fun startAndWaitForCallback(port: Int): Either<String, String>
}
```

**Implementation Strategy**:
1. Parse port from redirect_url config (e.g., "http://localhost:34432" → port 34432)
2. Create embedded Ktor server on that port
3. Define single route: `GET /` to handle OAuth callback
4. Extract `public_token` from query parameters (Plaid Link sends it as `?oauth_state_id=...&public_token=...`)
5. Use `CompletableDeferred<Either<String, String>>` to return value from suspend function
6. Server should:
   - Respond with success HTML page ("Authorization successful! You can close this window.")
   - Complete the deferred with the public token wrapped in Either.Right
   - Or complete with Either.Left if error occurs
7. Implement AutoCloseable.close() to stop server gracefully
8. Use Kotlin's `use { }` for automatic resource management

**URL Query Parameters from Plaid**:
- After user authorizes in browser, Plaid redirects to: `http://localhost:34432/?public_token=public-sandbox-xxx...`
- Need to extract `public_token` parameter

---

### Phase 5: Browser Launcher

**Component**: Platform-specific browser opener

**Files to Create**:
- `src/commonMain/kotlin/browser/BrowserLauncher.kt` - expect class/interface
- `src/macosArm64Main/kotlin/browser/BrowserLauncher.kt` - actual implementation

**Design**:
```kotlin
interface BrowserLauncher {
    suspend fun openUrl(url: String): Either<String, Unit>
}
```

**macOS Implementation**:
- Use Kotlin's `ProcessBuilder` (NO Java AWT)
- Execute: `open "https://cdn.plaid.com/link/v2/stable/link.html?token=<link_token>"`
- Return Either based on process exit code
- Alternative: use `NSWorkspace.sharedWorkspace().openURL()` via Kotlin/Native interop

**Recommendation**: Use ProcessBuilder with `open` command for simplicity

---

### Phase 6: Integration in AccountCommandInterpreter

**Files to Modify**:
- `src/commonMain/kotlin/interpreter/AccountCommandInterpreterImpl.kt`
- `src/commonMain/kotlin/interpreter/InterpreterFactory.kt`

**Flow Changes in `addAccount` method**:

```kotlin
suspend fun addAccount(username: String): Either<String, Unit> = either {
    val config = configDao.loadPlaidConfig().bind()
    val linkToken = plaidService.createLinkToken(username).bind()
    val linkUrl = "https://cdn.plaid.com/link/v2/stable/link.html?token=$linkToken"

    val port = extractPortFromUrl(config.redirect_url)

    // Use AutoCloseable pattern for server
    oauthServer.use { server ->
        val serverDeferred = async { server.startAndWaitForCallback(port) }

        browserLauncher.openUrl(linkUrl).bind()

        val publicToken = serverDeferred.await().bind()
        val accessTokenResponse = plaidService.exchangePublicToken(publicToken).bind()
        val accountsResponse = plaidService.getAccounts(accessTokenResponse.access_token).bind()

        val bankDetails = BankDetails(
            name = accountsResponse.item.institution_id ?: "Unknown Bank",
            username = username,
            accounts = accountsResponse.accounts.map { /* map accounts */ }
        )
        accountDao.save(bankDetails).bind()

        val plaidToken = PlaidToken(
            bankName = bankDetails.name,
            accessToken = accessTokenResponse.access_token,
            itemId = accessTokenResponse.item_id,
            createdAt = Clock.System.now()
        )
        tokenDao.save(plaidToken)
    }
}

// Note: Bank parameter removed - user selects institution in Plaid Link UI
// Error handling: all errors bubble up via bind(), handled at call site
```

**Factory Updates**:
```kotlin
object InterpreterFactory {
    fun get(environment: String): Interpreter {
        val httpClient = HttpClientFactory.create()
        val configDao = JsonConfigDao()
        val plaidService = PlaidServiceImpl(httpClient, configDao)
        val accountDao = JsonAccountDaoImpl()
        val tokenDao = JsonTokenDaoImpl()
        val oauthServer = OAuthRedirectServer()
        val browserLauncher = BrowserLauncher()

        val accountInterpreter = AccountCommandInterpreterImpl(
            plaidService, accountDao, tokenDao, configDao, oauthServer, browserLauncher
        )

        return Interpreter(accountInterpreter)
    }
}
```

---

### Phase 7: Error Handling & Edge Cases

**Error Scenarios to Handle**:

1. **Config file missing or malformed**:
   - Return `Either.Left("Failed to load Plaid configuration: ...")`

2. **Network errors during API calls**:
   - Wrap exceptions: "Failed to connect to Plaid API: connection timeout"

3. **Plaid API errors**:
   - Parse error response and return meaningful message
   - Example: "Invalid credentials" or "Institution not available"

4. **OAuth server port already in use**:
   - Return error suggesting user check if port is available

5. **User closes browser without authorizing**:
   - Timeout after 5 minutes
   - Return `Either.Left("Authorization timed out")`

6. **Public token exchange fails**:
   - Return Plaid error message

**User Experience**:
- Print clear messages at each step:
  - "Opening browser for authorization..."
  - "Waiting for authorization..."
  - "Authorization successful! Fetching account details..."
  - "Account added successfully!"

---

## Build Configuration

**Dependencies to Add** (in `build.gradle.kts`):

```kotlin
// Ktor server for OAuth redirect
implementation("io.ktor:ktor-server-core:2.3.12")
implementation("io.ktor:ktor-server-cio:2.3.12") // or netty
implementation("io.ktor:ktor-server-html:2.3.12") // for HTML response
```

**Already Available**:
- ktor-client-core, ktor-client-darwin
- ktor-client-content-negotiation
- ktor-serialization-kotlinx-json
- arrow-kt
- kotlinx.serialization
- kotlinx.coroutines

---

## Testing Strategy

**Unit Tests** (to be implemented):
- Mock PlaidService in interpreter tests
- Test Either error paths
- Test config parsing
- Test CompletableDeferred in OAuthRedirectServer

**Manual Testing Reference** (for user - DO NOT execute):
1. Ensure `~/.spndx/app-data-sandbox.json` exists with valid credentials
2. Run: `./gradlew build`
3. Run: `./build/bin/macosArm64/releaseExecutable/financial-advisor.kexe account add --username testuser`
4. Verify browser opens with Plaid Link
5. Select bank (Chase, etc.) in Plaid Link UI
6. Complete authorization in browser with test credentials
7. Verify redirect to localhost
8. Verify account saved to `~/.spndx/banks.json`
9. Verify token saved to `~/.spndx/tokens.json`

**Plaid Sandbox Test Credentials**:
- Username: `user_good`, Password: `pass_good`
- MFA code: `1234` (if prompted)

---

## Security Considerations

1. **Credentials Storage**:
   - Already handled: files in `~/.spndx/` have 0600 permissions (owner only)
   - Never log access tokens or secrets

2. **HTTP Server**:
   - Only listen on localhost (127.0.0.1)
   - Shutdown immediately after receiving token
   - Don't expose any other routes

3. **OAuth State Validation**:
   - Consider adding state parameter validation if Plaid supports it
   - Prevents CSRF attacks

4. **HTTPS**:
   - All Plaid API calls use HTTPS
   - Local redirect server uses HTTP (standard for localhost OAuth)

---

## File Structure Summary

```
src/
├── commonMain/kotlin/
│   ├── browser/
│   │   └── BrowserLauncher.kt (expect)
│   ├── config/
│   │   ├── PlaidConfig.kt (data class)
│   │   ├── ConfigDao.kt (interface)
│   │   └── JsonConfigDao.kt (expect class)
│   ├── plaid/
│   │   ├── HttpClientFactory.kt (new)
│   │   ├── PlaidModels.kt (extend with request models)
│   │   ├── PlaidServiceImpl.kt (new - real implementation)
│   │   └── OAuthRedirectServer.kt (expect)
│   └── interpreter/
│       ├── AccountCommandInterpreterImpl.kt (modify)
│       └── InterpreterFactory.kt (modify)
└── macosArm64Main/kotlin/
    ├── browser/
    │   └── BrowserLauncher.kt (actual)
    ├── config/
    │   └── JsonConfigDao.kt (actual)
    └── plaid/
        └── OAuthRedirectServer.kt (actual)
```

---

## Implementation Order

1. ✅ **Configuration Management** - Foundation for everything
2. ✅ **HTTP Client Factory** - Needed by PlaidService
3. ✅ **Plaid Request Models** - Needed by PlaidService
4. ✅ **Real PlaidService Implementation** - Core functionality
5. ✅ **Browser Launcher** - User interaction
6. ✅ **OAuth Redirect Server** - Receive callback
7. ✅ **Interpreter Integration** - Wire everything together
8. ✅ **Factory Updates** - Dependency injection
9. ✅ **Testing** - Verify end-to-end flow

---

## Success Criteria

- [ ] User can run `account add --bank chase --username <name>`
- [ ] Browser opens automatically with Plaid Link
- [ ] User completes authorization in browser
- [ ] App receives public token via redirect
- [ ] Public token is exchanged for access token
- [ ] Access token is saved to `~/.spndx/tokens.json`
- [ ] Bank details are saved to `~/.spndx/banks.json`
- [ ] All operations use `Either` for error handling
- [ ] No exceptions thrown in happy path
- [ ] Clear error messages for all failure cases

---

## Notes

- **Plaid Link URL Format**: `https://cdn.plaid.com/link/v2/stable/link.html?token=<link_token>`
- **Redirect URL Format**: Plaid will redirect to `http://localhost:34432/?public_token=<token>&oauth_state_id=<id>`
- **Environment**: Using Plaid Sandbox environment for testing
- **Multiplatform**: Current focus is macOS arm64, but structure supports future platforms

---

## Risk Mitigation

**Risk**: OAuth redirect server port conflict
**Mitigation**: Check if port is available before starting, return clear error

**Risk**: Browser doesn't open
**Mitigation**: Log the URL so user can manually open it

**Risk**: User abandons flow
**Mitigation**: Implement 5-minute timeout, clean up server

**Risk**: Plaid API changes
**Mitigation**: Version API calls, handle unknown fields gracefully with lenient JSON parsing

---

This plan follows the existing architectural patterns (expect/actual, Either, suspend functions, interface-based design) while adding the necessary OAuth flow components.

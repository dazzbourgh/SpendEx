package config

/**
 * Application-wide constants extracted from hardcoded values.
 * Organized by category for easy maintenance and configuration.
 */
object Constants {
    object App {
        const val NAME = "spndx"
        const val DISPLAY_NAME = "SpendEx"
        const val DESCRIPTION = "SpendEx - CLI tool for managing financial accounts and transactions"
        const val ENVIRONMENT_SANDBOX = "sandbox"
    }

    object FileSystem {
        const val HOME_ENV_VAR = "HOME"
        const val APP_DIR_NAME = ".spndx"
        const val TOKENS_FILE_NAME = "tokens.json"
        const val TRANSACTIONS_DIR_NAME = "transactions"

        const val DIR_PERMISSIONS = 0x1C0u // 0700 octal (UShort for mkdir)
        const val FILE_PERMISSIONS = 0x180u // 0600 octal (UShort for chmod)
        const val BUFFER_SIZE = 65536 // 64KB
        const val ELEMENT_SIZE = 1uL // ULong for fread/fwrite

        const val FILE_MODE_READ = "r"
        const val FILE_MODE_WRITE = "w"

        object ErrorMessages {
            const val HOME_NOT_SET = "HOME environment variable not set"
            const val CANNOT_OPEN_FILE = "Cannot open file for writing"
            const val CONFIG_NOT_FOUND = "Plaid configuration file not found at"
            const val CONFIG_LOAD_FAILED = "Failed to load Plaid configuration"
        }
    }

    object Plaid {
        const val LINK_URL = "https://cdn.plaid.com/link/v2/stable/link.html"
        const val CLIENT_NAME = "SpendEx"
        const val LANGUAGE = "en"
        const val UNKNOWN_BANK = "Unknown Bank"

        // Single user ID for Plaid Link token creation
        // NOTE: This assumes only one user per Plaid app instance.
        // Will not work if SpendEx CLI is used by multiple users through the same Plaid app.
        const val CLIENT_USER_ID = "SpendEx"

        object Endpoints {
            const val LINK_TOKEN_CREATE = "/link/token/create"
            const val PUBLIC_TOKEN_EXCHANGE = "/item/public_token/exchange"
            const val ACCOUNTS_GET = "/accounts/get"
            const val TRANSACTIONS_SYNC = "/transactions/sync"
        }

        object ErrorMessages {
            const val LINK_TOKEN_FAILED = "Failed to create link token"
            const val TOKEN_EXCHANGE_FAILED = "Failed to exchange public token"
            const val ACCOUNTS_GET_FAILED = "Failed to get accounts"
            const val TRANSACTIONS_SYNC_FAILED = "Failed to sync transactions"
        }
    }

    object OAuth {
        const val SERVER_HOST = "127.0.0.1"
        const val DEFAULT_PORT = 34432
        const val REDIRECT_URL = "http://$SERVER_HOST:$DEFAULT_PORT"
        const val ROOT_PATH = "/"
        const val PUBLIC_TOKEN_PARAM = "public_token"
        const val OAUTH_STATE_ID_PARAM = "oauth_state_id"
        const val PORT_DELIMITER = ":"

        const val TIMEOUT_MILLIS = 300_000L // 5 minutes
        const val SHUTDOWN_GRACE_MILLIS = 1000L
        const val SHUTDOWN_TIMEOUT_MILLIS = 2000L

        object Html {
            const val SUCCESS_PAGE = """<!DOCTYPE html>
<html>
<head><title>Authorization Successful</title></head>
<body>
    <h1>Authorization Successful!</h1>
    <p>You can close this window and return to the terminal.</p>
</body>
</html>"""

            const val FAILURE_PAGE = """<!DOCTYPE html>
<html>
<head><title>Authorization Failed</title></head>
<body>
    <h1>Authorization Failed</h1>
    <p>No public token received. Please try again.</p>
</body>
</html>"""

            const val OAUTH_CONTINUATION_PAGE = """<!DOCTYPE html>
<html>
<head>
    <title>Continuing Authorization</title>
    <script>
        window.location.href = '%s';
    </script>
</head>
<body>
    <h1>Continuing OAuth Authorization...</h1>
    <p>You will be redirected automatically. If not, <a href="%s">click here</a>.</p>
</body>
</html>"""
        }

        object Messages {
            const val WAITING_FOR_AUTH = "Waiting for authorization..."
            const val NO_TOKEN_RECEIVED = "No public token received in callback"
            const val TIMEOUT = "Authorization timed out after 5 minutes"
            const val SERVER_START_FAILED = "Failed to start OAuth redirect server"
            const val SERVER_CLOSED_EARLY = "Server closed before receiving callback"
        }
    }

    object Commands {
        object Accounts {
            const val NAME = "accounts"
            const val HELP = "Manage financial accounts"

            object Add {
                const val NAME = "add"
                const val HELP = "Add a new financial account. You will select the bank and login in your browser."
            }

            object List {
                const val NAME = "list"
                const val HELP = "List all added accounts"
            }
        }

        object Plaid {
            const val NAME = "plaid"
            const val HELP = "Manage Plaid configuration"

            object Configure {
                const val NAME = "configure"
                const val HELP = "Configure Plaid API credentials"
                const val CLIENT_ID_HELP = "Plaid client ID"
                const val CLIENT_SECRET_HELP = "Plaid client secret"
            }
        }

        object Transactions {
            const val NAME = "transactions"
            const val HELP = "Manage and view transactions"

            object List {
                const val NAME = "list"
                const val HELP = "List transactions with optional filters"
                const val FROM_HELP = "Start date (YYYY-MM-DD)"
                const val TO_HELP = "End date (YYYY-MM-DD)"
                const val INSTITUTION_HELP = "Filter by institution name (can be specified multiple times)"
            }
        }

        object ErrorMessages {
            const val UNKNOWN_ENVIRONMENT = "Unknown environment"
            const val ACCOUNT_ADD_FAILED = "Error adding account"
            const val ACCOUNT_ADD_SUCCESS = "Account added successfully!"
            const val ACCOUNT_LIST_FAILED = "Error listing accounts"
            const val PLAID_CONFIGURE_FAILED = "Error configuring Plaid"
            const val PLAID_CONFIGURE_SUCCESS = "Plaid configuration saved successfully!"
            const val TRANSACTION_LIST_FAILED = "Error listing transactions"
            const val INVALID_DATE_FORMAT = "Invalid date format. Expected YYYY-MM-DD"
            const val INVALID_DATE_RANGE = "Start date must be before or equal to end date"
        }
    }

    object Browser {
        const val MACOS_OPEN_COMMAND = "open"
        const val SUCCESS_EXIT_CODE = 0

        object ErrorMessages {
            const val COMMAND_FAILED = "Failed to execute open command"
            const val LAUNCH_FAILED = "Browser launch failed with exit code"
            const val OPEN_FAILED = "Failed to open browser"
        }
    }

    object Resources {
        const val LINK_HTML_FILE = "link.html"
    }
}

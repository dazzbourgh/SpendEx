package interpreter

import browser.BrowserLauncher
import browser.BrowserLauncherImpl
import plaid.OAuthRedirectServer
import plaid.OAuthRedirectServerImpl

actual fun createBrowserLauncher(): BrowserLauncher = BrowserLauncherImpl()

actual fun createOAuthRedirectServer(): OAuthRedirectServer = OAuthRedirectServerImpl()

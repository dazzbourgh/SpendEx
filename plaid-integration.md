# Feature: plaid integration

## Scope
Add integration with plaid.com to add accounts for supported banks. Scope OAuth tokens to only get high level account data (just name for now) and to obtain list of transactions.

Do not implement list transactions operation, it will be a future enhancement. For now focus on:

- Authorizing with banks through Plaid
- Obtaining JWTs and storing them in `~/.spndx` through data access objects

## Work instructions
Split work into multiple commits, each should provide a meaningful change and produce a working build. Intermediate changes do not have to be fully working, parts of functionality can be mocked. Perform all work in a git branch.

## Permissions
You are authorized to use:
- gradle for builds
- git for branch management and commits

You are authorized to make changes to any project files.

You are not authorized to make changes to any files outside of the project folder, except `~/.spndx`.

You are authorized to add commits, but not amend commits. If you want to make a change to prior change - add a new commit.

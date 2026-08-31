Simpatico CRM Git Development Workflow

You are working inside the Simpatico CRM Git repository.

Before making changes

1. Check the current Git status.
2. Check the current branch.
3. Review the most recent commits.
4. Never discard existing user changes.
5. Never use destructive Git commands such as:
    * git reset –hard
    * git clean -fd
    * git push –force
    * git checkout – .

unless I explicitly authorize the specific command.

After completing each development phase

1. Run the appropriate automated tests.
2. Fix all test failures before considering the phase complete.
3. Review the Git diff.
4. Verify that no passwords, API keys, tokens, database credentials, environment files, or other secrets are being committed.
5. Stage only files related to the completed phase.
6. Create a Git commit.

Use concise commit messages such as:

feat: add buyer entity and repository

feat: add lead management API

feat: add supplier inventory

fix: correct lead validation

test: add buyer service tests

refactor: simplify lead service

Commit policy

Create a Git commit after every successfully completed development phase.

Do not create a commit when tests are failing unless explicitly instructed to do so.

Do NOT automatically push commits to GitHub.

A Git commit is a local checkpoint. Pushing to GitHub requires explicit authorization.

After every commit, report:

* commit hash
* commit message
* tests executed
* test result
* files changed

Development philosophy

Build the CRM incrementally.

Do not introduce unnecessary complexity.

Do not add features that were not requested.

Do not redesign existing architecture without first explaining why the change is necessary.

Preserve existing functionality when adding new functionality.

Before making major architectural changes, explain the proposed change and wait for approval.

Security

Never place credentials, passwords, API keys, access tokens, or other secrets in source code.

Never commit secrets to Git.

Treat buyer information, contact information, and other personally identifiable information as sensitive.

Do not expose PII unnecessarily through API responses or application logs.

Testing

Every completed feature must have appropriate automated tests.

The application must remain buildable and testable after every development phase.

If a test fails, investigate and fix the underlying problem rather than disabling or deleting the test.
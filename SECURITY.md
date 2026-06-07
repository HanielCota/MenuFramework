# Security Policy

## Supported versions

MenuFramework is pre-1.0. Security fixes target the latest released version and the `main` branch.

| Version | Supported |
|---------|-----------|
| 0.1.x   | ✅        |
| < 0.1   | ❌        |

## Reporting a vulnerability

Please do not open a public issue for security problems.

Report privately through GitHub's [private vulnerability reporting](https://github.com/HanielCota/MenuFramework/security/advisories/new),
or by email to **pessoal@hanielfialho.com**.

Include the affected version, a description of the impact, and steps to reproduce when possible.
You can expect an acknowledgement within a few days. Once a fix is available it will be released and
the reporter credited, unless anonymity is requested.

## Scope

This is a server-side library for Paper and Folia plugins. Reports about how player-controlled input
reaches a menu (for example placeholder text rendered into another viewer's inventory, or item
movement through an open menu) are in scope.

# [2.0.0-dev.1](https://github.com/FTX-Aseca/libra-wallet/compare/v1.0.0...v2.0.0-dev.1) (2025-05-28)


### Bug Fixes

* **CD:** Updated CD workflow to trigger on published releases ([a71cc99](https://github.com/FTX-Aseca/libra-wallet/commit/a71cc99d5c8e82da770ba8fea879ad079ac1f9e7))


### Features

* **account:** add account details endpoint ([278a946](https://github.com/FTX-Aseca/libra-wallet/commit/278a946daf51e7b65cef1e7094657db47dffe14d))
* **Docker:** Added Docker Compose ([4b0276d](https://github.com/FTX-Aseca/libra-wallet/commit/4b0276d1d884b6c5580d88c517189e069dd84715))


### BREAKING CHANGES

* **CD:** none
Fixes: none
* **Docker:** Now can use Docker Compose
Fixes: none

### Features

* **account:** add account details endpoint ([278a946](https://github.com/FTX-Aseca/libra-wallet/commit/278a946daf51e7b65cef1e7094657db47dffe14d))
# 1.0.1 (2025-05-28)


### Bug Fixes

* **CD:** Updated CD workflow to trigger on published releases ([a71cc99](https://github.com/FTX-Aseca/libra-wallet/commit/a71cc99d5c8e82da770ba8fea879ad079ac1f9e7))
* **security:** permit callback endpoints without authentication ([a77ac48](https://github.com/FTX-Aseca/libra-wallet/commit/a77ac48cdd3755c4b86e45788440dbf13adb7ef8))


### Features

* **Docker:** Added Docker Compose ([4b0276d](https://github.com/FTX-Aseca/libra-wallet/commit/4b0276d1d884b6c5580d88c517189e069dd84715))
* **CI:** Modified pre-commit hook. ([968e27a](https://github.com/FTX-Aseca/libra-wallet/commit/968e27a785f353816aa51c80f93e67294a9a7609)), closes [#6](https://github.com/FTX-Aseca/libra-wallet/issues/6) [#11](https://github.com/FTX-Aseca/libra-wallet/issues/11)
* **transaction:** add error handling for invalid amounts ([d2581c2](https://github.com/FTX-Aseca/libra-wallet/commit/d2581c2f25e7a4d385a4c8dc9a23728f961ff4e1))
* **transaction:** add validation for transaction amount ([3ecd180](https://github.com/FTX-Aseca/libra-wallet/commit/3ecd180d02385066a79f3d2194f37c56ca5a8901))
* **transaction:** include amount in transaction response ([6eb655e](https://github.com/FTX-Aseca/libra-wallet/commit/6eb655e549ad80d8c8a5f3557f5a45f309bd9635))


### BREAKING CHANGES

* **Docker:** Can now use Docker Compose

# [1.0.0-dev.2](https://github.com/FTX-Aseca/libra-wallet/compare/v1.0.0-dev.1...v1.0.0-dev.2) (2025-05-24)


### Bug Fixes

* **security:** permit callback endpoints without authentication ([a77ac48](https://github.com/FTX-Aseca/libra-wallet/commit/a77ac48cdd3755c4b86e45788440dbf13adb7ef8))
* **testing:** Banchu formatting ([d0ccfe8](https://github.com/FTX-Aseca/libra-wallet/commit/d0ccfe808bc57558eb1a08e3d2d15c3cbad1d9e9))


### Features

* **transaction:** add error handling for invalid amounts ([d2581c2](https://github.com/FTX-Aseca/libra-wallet/commit/d2581c2f25e7a4d385a4c8dc9a23728f961ff4e1))
* **transaction:** add validation for transaction amount ([3ecd180](https://github.com/FTX-Aseca/libra-wallet/commit/3ecd180d02385066a79f3d2194f37c56ca5a8901))
* **transaction:** include amount in transaction response ([6eb655e](https://github.com/FTX-Aseca/libra-wallet/commit/6eb655e549ad80d8c8a5f3557f5a45f309bd9635))

# 1.0.0-dev.1 (2025-05-22)


### Features

* **CI:** Modified pre-commit hook. ([968e27a](https://github.com/FTX-Aseca/libra-wallet/commit/968e27a785f353816aa51c80f93e67294a9a7609)), closes [#6](https://github.com/FTX-Aseca/libra-wallet/issues/6) [#11](https://github.com/FTX-Aseca/libra-wallet/issues/11)


### BREAKING CHANGES

* **CI:** none

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project setup
- Docker configuration for development and production environments
- GitHub Actions workflows for CI/CD
- Semantic versioning support

### Changed
- None

### Deprecated
- None

### Removed
- None

### Fixed
- None

### Security
- None

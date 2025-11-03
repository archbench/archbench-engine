## ArchBench Engine OpenAPI Contract

This directory contains the contract-first OpenAPI definition for the ArchBench Engine service. The `openapi.yaml` file is the source of truth for external clients and should be updated whenever the API surface changes.

### How to evolve the spec

1. Discuss proposed changes with both engine and frontend owners to avoid surprises.
2. Update `openapi.yaml`, keeping versions semantically meaningful.
3. Regenerate or adjust DTOs, server handlers, and client code to match the contract.
4. Add tests that cover the new or modified endpoints.
5. Commit the spec update and implementation changes together to keep them in sync.

# Plan to Address Public Release Blockers and Cleanup Items

This plan details the changes required to address the blockers and clean up the codebase for a public release.

## User Review Required & Action Items

> [!IMPORTANT]
> - **Credentials Discarding & Warning**: We will replace hardcoded Commercetools credentials in `test_idempotency.py` with environment variables (`CTP_PROJECT_KEY`, `CTP_CLIENT_ID`, etc.) and skip the test if not provided. We will issue a strong recommendation to the user to rotate/revoke these credentials on their Commercetools and Stripe dashboards immediately.
> - **Predictable Container Names**: We will add explicit `container_name: foundational-mcp-server` to the Docker Compose file and update `MCP_CLIENT_SETUP.md` to use this unified container name instead of the brittle directory-inferred name `backend-foundational-mcp-server-1`.
> - **API Gateway Proxying in Nginx**: We will configure `nginx.conf` to proxy `/api/` traffic directly to the internal API Gateway service `http://api-gateway:8080/api/` inside the Docker Compose network.
> - **JDK Compiler Target & Lombok Compile Fix**: 
>   - We will set the compiler `release` target to `17` in all `pom.xml` files.
>   - We will configure the `maven-compiler-plugin` to register the Lombok annotation processor path, resolving compile failures for newer JDK versions.
>   - We will update the docs recommending JDK 17.
> - **Security & Sandboxing Disclaimers**: We will add a prominent warning disclaimer to the root README: *"This is a local sandbox demo, not a production-ready commerce backend."*
> - **Postman Headers & Payload Cleanups**: We will update the Postman collection to send `Content-Type: application/json` headers and submit credentials in a JSON POST body rather than URL query parameters.

## Proposed Changes

### Foundational MCP Server (`phase-1/foundational-mcp-server`)

#### [MODIFY] [test_idempotency.py](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/foundational-mcp-server/tests/test_idempotency.py)
- Replace hardcoded `PROJECT_KEY`, `CLIENT_ID`, `CLIENT_SECRET`, `AUTH_URL`, and `API_URL` with env-based queries.
- Add checks to skip tests with `pytest.skip` if the required credentials are not found in the environment.

#### [MODIFY] [requirements.txt](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/foundational-mcp-server/requirements.txt)
- Pin required Python dependencies: `mcp`, `httpx`, `uvicorn`, `starlette`, `python-dotenv`, `pytest`, `pytest-asyncio`.
- Completely remove `openai` as it is not used in the MCP server.

---

### Backend Microservices (`phase-1/backend`)

#### [MODIFY] [pom.xml](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/backend/pom.xml)
- Add `maven-compiler-plugin` configuration defining target/source release of `17` and registration of the Lombok annotation processor.

#### [MODIFY] [docker-compose.yml](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/backend/docker-compose.yml)
- Add `container_name: foundational-mcp-server` under the `foundational-mcp-server` service.

#### [MODIFY] [.github/workflows/build.yml](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/.github/workflows/build.yml) (Root)
- Change `mvn -B clean verify -DskipTests` to `mvn -B clean verify` to ensure backend unit/mock tests run on continuous integration.

#### [MODIFY] [commercetools_journey.postman_collection.json](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/docs/postman/commercetools_journey.postman_collection.json)
- Update "Register Customer" and "Login Customer" requests to use a `POST` request with a JSON raw body and `Content-Type: application/json` header, removing email/password from the URL query parameters.

---

### Frontend Storefront (`phase-1/frontend`)

#### [MODIFY] [nginx.conf](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/frontend/nginx.conf)
- Add `/api/` routing block proxying requests to `http://api-gateway:8080/api/`.

#### [NEW] [.eslintrc.cjs](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/frontend/.eslintrc.cjs)
- Create a standard ESLint configuration file for the React Vite environment.

#### [MODIFY] [CheckoutSuccess.jsx](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/frontend/src/pages/CheckoutSuccess.jsx)
- Correct the endpoint path for vaulting payment methods from `/customers/{id}/payments` to `/customers/{id}/payment-methods`.
- Send requests using a `POST` method containing a JSON body payload instead of query parameters, with proper headers.

---

### Project Documentation

#### [MODIFY] [README.md](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/README.md) (Root)
- Add a prominent warning disclaimer: *"This is a local sandbox demo, not a production-ready commerce backend."*
- Recommend JDK 17 for local builds.

#### [MODIFY] [MCP_CLIENT_SETUP.md](file:///Users/srinivasbellarichenna/Personal/dev/genai-projects/antigravity_workspace/commercetools-mcp-demo/phase-1/docs/MCP_CLIENT_SETUP.md)
- Update Docker attachment options to use `foundational-mcp-server` as the explicit container name instead of `backend-foundational-mcp-server-1`.

---

## Verification Plan

### Automated Tests
- Run maven test suite locally:
  ```bash
  mvn clean test -f phase-1/backend/pom.xml
  ```
- Run ESLint to verify configuration correctness:
  ```bash
  npm run lint --prefix phase-1/frontend
  ```
- Build the frontend project:
  ```bash
  npm run build --prefix phase-1/frontend
  ```

### Docker Verification
- Validate the Compose configuration file and build:
  ```bash
  cd phase-1/backend
  docker compose config
  docker compose build
  ```

### Secret Scan Verification
- Perform a manual scan for credentials and check files for hardcoded test patterns prior to public release.

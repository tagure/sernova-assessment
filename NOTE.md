# Sernova Technical Test – Notes

## Issues Identified

- `Person` and `Address` entities did not define primary keys, so Hibernate could not manage them and the application failed to start correctly.
- No database driver or datasource configuration was present, so Spring Boot could not create a `DataSource` and the app exited instead of serving requests.
- The `/api/persons-with-addresses` endpoint used lazy loading of addresses, leading to an N+1 query pattern and unnecessary database overhead.
- The endpoint returning persons with addresses also produced a large JSON payload in a single response, adding serialization and transfer cost.

## Changes Made (and Why)

- Added `@Id` and `@GeneratedValue` fields to both `Person` and `Address`, making them valid JPA entities so Hibernate can persist and load them reliably.
- Configured an in-memory H2 database and set `spring.datasource.*` and `spring.jpa.*` properties, keeping the project self-contained and easy to run locally.
- Introduced DTOs (`PersonDto`, `AddressDto`, `PersonWithAddressesDto`) and updated the REST endpoints to return DTOs instead of entities, simplifying the API surface and JSON structure.
- Implemented the `POST /api/seed/people` endpoint to insert 10,000 persons with 2 addresses each using batched `saveAll` calls, providing a consistent dataset for testing and performance measurement.

## Performance Improvement Approach

- Replaced the N+1 loading pattern with a single `LEFT JOIN FETCH` query in `PersonRepository.findAllWithAddresses`, so persons and their addresses are loaded in one join query instead of many individual address queries.
- Kept the one-to-many mapping intact and performed the mapping to DTOs in the controller to reduce JSON size and avoid exposing internal JPA details.
- Optionally added simple API-level pagination (page/size parameters) on `/api/persons-with-addresses` to limit the number of persons returned per call and further reduce response size when needed.

## Measured Impact (Before / After)

- **Before optimization:**
  - `GET /api/persons` → `time_total ≈ 0.861781`
  - `GET /api/persons-with-addresses` → `time_total ≈ 0.885369`

- **After optimization (join-fetch + DTOs and related fixes):**
  - `GET /api/persons` → `time_total ≈ 0.142674`
  - `GET /api/persons-with-addresses` → `time_total ≈ 0.489564`

These measurements show a clear reduction in latency for both endpoints, with `/api/persons` improving by roughly a factor of six and `/api/persons-with-addresses` nearly halving in total time while still returning the full logical dataset.

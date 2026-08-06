# SaaS pilot refactor plan

## Current diagnosis

- `AppUser` has no global authority. Company roles (`OWNER`, `ADMIN`, `MANAGER`, `FINANCE`, `WORKER`) are stored on `CompanyMembership`.
- `POST /companies` is reachable by any authenticated user and creates a membership for the current user. It does not model platform-led customer provisioning.
- The JWT carries the selected `companyId` and company `role`, while `JwtAuthenticationFilter` already revalidates active membership and active company on every request.
- `POST /auth/select-company` and `GET /auth/me/companies` already validate membership scope and will be preserved for compatibility.
- Company reads are membership-scoped in `CompanyService`, but inactive companies need to be rejected for normal users and platform operations need a separate explicit path.
- `CompanyMapper` currently follows the DTO order (`active`, then `contractorInvoiceGstEnabled`); a regression test must cover opposite boolean values.
- Invitations provision ordinary company members on acceptance. Initial owner provisioning is a separate administrative transaction so the owner membership can be active immediately.

## Target endpoints

| Endpoint | Access | Purpose |
| --- | --- | --- |
| `POST /platform/companies` | `PLATFORM_ADMIN` | Provision a company and its initial active owner membership atomically |
| `GET /platform/companies` | `PLATFORM_ADMIN` | List all platform companies, including inactive customers |
| `GET /auth/me/companies` | Authenticated user | List only the caller's active memberships and active companies |
| `POST /auth/select-company` | Authenticated user | Issue a token for a company the caller actively belongs to |
| `/companies/**` | Platform admin or active company membership | Company-scoped operations, subject to company role and active-company checks |

The legacy `POST /companies` route will no longer be available to normal users. The platform route is the canonical provisioning API.

## Planned changes

1. Add `SystemRole { PLATFORM_ADMIN, USER }` to `AppUser` with a safe `USER` default for existing rows.
2. Add an explicit platform provisioning DTO, controller and transactional service.
3. Resolve or create the owner user, then create or reactivate the unique `OWNER` membership in the same transaction as the company.
4. Add explicit platform authorities to JWT authentication while retaining company roles in `CompanyMembership`.
5. Centralize active membership/company checks for normal company access and retain safe not-found behavior across tenants.
6. Keep the existing invitation flow for ordinary members and avoid changing work-log, snapshot, GST, invoice or PDF calculations.

## Migrations

- `V10__add_app_user_system_role.sql`: add `system_role`, backfill existing users as `USER`, and enforce a safe non-null default.

The first platform administrator will be configured operationally after migration with a controlled SQL update to a known account. No company `OWNER` will be promoted automatically.

## Compatibility and risks

- Existing company role names are preserved (`ADMIN` and `FINANCE` remain the current equivalents of the requested administrative roles).
- `/auth/me/companies` and `/auth/select-company` remain available to avoid frontend breakage.
- Existing JWTs without a global role remain normal `USER` tokens and cannot access platform routes.
- Existing company memberships and financial history are not rewritten.
- Provisioning a new owner requires a temporary password in the administrative request when the owner account does not already exist; the password is never returned in the response.

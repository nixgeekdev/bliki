package dev.nixgeek.bliki.service.shared.resources

object Routes {
    private const val CONTEXT_PATH = "/service"

    private const val ADMIN_PATH = "/admin"
    private const val API_V1_PATH = "/api/v1"
    private const val DOCS_PATH = "/docs"
    private const val HEALTH_PATH = "/health"
    private const val LOGIN_PATH = "/login"
    private const val MANAGE_PATH = "/manage"
    private const val PUBLIC_PATH = "/public"

    internal const val BLIKI_PATH = "/bliki"
    internal const val ENTRY_PATH = "/entry"
    internal const val GENERATOR_PATH = "/generator"
    internal const val IDENTITY_PATH = "/identity"
    internal const val PROFILE_PATH = "/profile"
    internal const val ROLE_PATH = "/role"
    internal const val REVISION_PATH = "/revision"
    internal const val TAG_PATH = "/tag"

    internal const val ULID_PARAM = "/{ulid}"

    // ***************************************  DOCS  *************************************** //

    // -> /service/docs
    const val DOCS_BASE = "$CONTEXT_PATH$DOCS_PATH"

    // ***************************************  LOGIN  ************************************** //

    // -> /service/login
    const val LOGIN_BASE = "$CONTEXT_PATH$LOGIN_PATH"

    // *************************************** MANAGE *************************************** //

    // -> /service/manage
    const val MANAGE_BASE = "$CONTEXT_PATH$MANAGE_PATH"

    // -> /service/manage/health
    const val MANAGE_HEALTH_BASE = "$MANAGE_BASE$HEALTH_PATH"

    // *************************************** PUBLIC *************************************** //

    // -> /service/public
    const val PUBLIC_BASE = "$CONTEXT_PATH$PUBLIC_PATH"

    // -> /service/public/api/v1
    const val PUBLIC_API_V1_BASE = "$PUBLIC_BASE$API_V1_PATH"

    // -> /service/public/api/v1/bliki
    const val PUBLIC_BLIKI_BASE = "$PUBLIC_API_V1_BASE$BLIKI_PATH"

    // -> /service/public/api/v1/entry
    const val PUBLIC_ENTRY_BASE = "$PUBLIC_API_V1_BASE$ENTRY_PATH"

    // -> /service/public/api/v1/generator
    const val PUBLIC_GENERATOR_BASE = "$PUBLIC_API_V1_BASE$GENERATOR_PATH"

    // -> /service/public/api/v1/identity
    const val PUBLIC_IDENTITY_BASE = "$PUBLIC_API_V1_BASE$IDENTITY_PATH"

    // -> /service/public/api/v1/profile
    const val PUBLIC_PROFILE_BASE = "$PUBLIC_API_V1_BASE$PROFILE_PATH"

    // -> /service/public/api/v1/revision
    const val PUBLIC_REVISION_BASE = "$PUBLIC_API_V1_BASE$REVISION_PATH"

    // -> /service/public/api/v1/role
    const val PUBLIC_ROLE_BASE = "$PUBLIC_API_V1_BASE$ROLE_PATH"

    // -> /service/public/api/v1/tag
    const val PUBLIC_TAG_BASE = "$PUBLIC_API_V1_BASE$TAG_PATH"

    // *************************************** ADMIN **************************************** //

    // -> /service/admin
    const val ADMIN_BASE = "$CONTEXT_PATH$ADMIN_PATH"

    // -> /service/admin/api/v1
    const val ADMIN_API_V1_BASE = "$ADMIN_BASE$API_V1_PATH"

    // -> /service/admin/api/v1/bliki
    const val ADMIN_BLIKI_BASE = "$ADMIN_API_V1_BASE$BLIKI_PATH"

    // -> /service/admin/api/v1/entry
    const val ADMIN_ENTRY_BASE = "$ADMIN_API_V1_BASE$ENTRY_PATH"

    // -> /service/admin/api/v1/generator
    const val ADMIN_GENERATOR_BASE = "$ADMIN_API_V1_BASE$GENERATOR_PATH"

    // -> /service/admin/api/v1/identity
    const val ADMIN_IDENTITY_BASE = "$ADMIN_API_V1_BASE$IDENTITY_PATH"

    // -> /service/admin/api/v1/profile
    const val ADMIN_PROFILE_BASE = "$ADMIN_API_V1_BASE$PROFILE_PATH"

    // -> /service/admin/api/v1/revision
    const val ADMIN_REVISION_BASE = "$ADMIN_API_V1_BASE$REVISION_PATH"

    // -> /service/admin/api/v1/role
    const val ADMIN_ROLE_BASE = "$ADMIN_API_V1_BASE$ROLE_PATH"

    // -> /service/admin/api/v1/tag
    const val ADMIN_TAG_BASE = "$ADMIN_API_V1_BASE$TAG_PATH"
}

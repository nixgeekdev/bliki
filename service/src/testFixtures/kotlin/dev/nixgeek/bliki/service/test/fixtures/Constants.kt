package dev.nixgeek.bliki.service.test.fixtures

object Constants {
    object Bliki {
        const val TITLE_01 = "My Bliki"
        const val TITLE_02 = "Another Bliki"
        const val TITLE_03 = "Yet Another Bliki"
        const val TITLE_04 = "Bliki 4"
        const val SUBTITLE = "A cross between a blog and a wiki"
        const val RIGHTS = "Copyright 2026 nixgeek.dev"
        const val BASE_URI = "https://example.test/bliki"
        const val LANG = "en/US"
    }

    object Generator {
        const val NAME_01 = "Bliki Generator"
        const val NAME_02 = "Bliki Generator Pro"
        const val NAME_03 = "Bliki Generator Enterprise"
        const val URI = "https://example.com/bliki-generator"
        const val VERSION_01 = "1.2.3"
        const val VERSION_02 = "3.2.1"
        const val VERSION_03 = "2.3.4"
    }

    object Identity {
        const val EMAIL_01 = "test@example.com"
        const val EMAIL_02 = "john.doe@example.com"
        const val EMAIL_03 = "jane.doe@example.com"
        const val HASH_01 = $$"{bcrypt}$2a$10$egsoWMzDrqR3aaE2oqpDJ.G9.ljiWVKmhH6Sbf0lFt583wW1SImkW"
        const val HASH_02 = $$"{bcrypt}$2a$10$9aB242Y0FJyxaKhuimUjPOUxq1qYmjtVihJRPa6hXL0nGvWMYyxka"
        const val HASH_03 = $$"{bcrypt}$2a$10$.Ghl.FxRyEpGQS51QKL4wedUa6pe/38fs6Gc9m9uC14MdDjEfMPsK"
    }

    object Profile {
        const val NAME_01 = "Bliki User"
        const val NAME_02 = "Bliki Admin"
        const val NAME_03 = "Bliki Contributor"
        const val AFFILIATION_01 = "Bliki Inc."
        const val AFFILIATION_02 = "NixGeek Dev Team"
        const val AFFILIATION_03 = "Open Source Community"
    }

    object Role {
        const val ROLE_01 = "ADMIN"
        const val LABEL_01 = "Administrator"

        const val ROLE_02 = "AUTHOR"
        const val LABEL_02 = "Author"

        const val ROLE_03 = "EDITOR"
        const val LABEL_03 = "Editor"
    }
}

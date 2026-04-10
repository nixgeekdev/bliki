package dev.nixgeek.bliki.service.test.fixtures

import dev.nixgeek.bliki.lib.data.ulid.toULID

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

    object Entry {
        const val LANG = "en/US"
        const val CONTENT_TYPE = "text/markdown"
        const val VISIBILITY = "PUBLIC"
        const val STATUS = "PUBLISHED"

        const val TITLE_01 = "Welcome to Bliki"
        const val TITLE_02 = "Getting Started"
        const val TITLE_03 = "Bliki in 5 Minutes"

        const val SLUG_01 = "welcome-to-bliki"
        const val SLUG_02 = "getting-started"
        const val SLUG_03 = "bliki-in-5-minutes"

        const val CONTENT_01 = """
            # Welcome to Bliki

            This is a sample Bliki page.
        """
        const val CONTENT_02 = """
            # Getting Started

            Here's how you do things in the Bliki application:

            1. Create a new Bliki
            2. Write some content
            3. Publish it!
        """
        const val CONTENT_03 = """
            # Bliki in 5 Minutes

            Follow these steps to get started with Bliki:

            1. Sign up for an account
            2. Create a new Bliki
            3. Start writing
            4. Publish your Bliki
        """
    }

    object Generator {
        const val NAME_01 = "Bliki Generator"
        const val NAME_02 = "Bliki Generator Pro"
        const val NAME_03 = "Bliki Generator Enterprise"

        const val VERSION_01 = "1.2.3"
        const val VERSION_02 = "3.2.1"
        const val VERSION_03 = "2.3.4"

        const val URI = "https://example.com/bliki-generator"
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

    object Revision {
        const val EVENT = "UPDATED"

        const val SUMMARY_01 = "Added footer"
        const val SUMMARY_02 = "Updated body"
        const val SUMMARY_03 = "Removed footer"

        const val DIFF_01 = """
            --- original
            +++ revised
            @@ -1,2 +1,3 @@
             Title
             Body
            +Footer
        """
        const val DIFF_02 = """
            --- original
            +++ revised
            @@ -1,3 +1,3 @@
             Title
            -Original body
            +Updated body
             Footer
        """
        const val DIFF_03 = """
            --- original
            +++ revised
            @@ -1,3 +1,2 @@
             Title
             Body
            -Footer
        """
    }

    object Role {
        const val ROLE_01 = "ADMIN"
        const val ROLE_02 = "AUTHOR"
        const val ROLE_03 = "EDITOR"

        const val LABEL_01 = "Administrator"
        const val LABEL_02 = "Author"
        const val LABEL_03 = "Editor"
    }

    object Tag {
        const val TAG_01 = "code"
        const val TAG_02 = "database"
        const val TAG_03 = "system"
        const val TAG_04 = "security"
        const val TAG_05 = "performance"
        const val TAG_06 = "testing"
        const val TAG_07 = "documentation"
        const val TAG_08 = "deployment"
        const val TAG_09 = "cloud"
        const val TAG_10 = "architecture"

        const val TAG_ID_01 = "01KNGC0BTCP30ZHBTEE6GAFG2M" // code
        const val TAG_ID_02 = "01KNPS404A77BAH4GSWER54JFQ" // database
        const val TAG_ID_03 = "01KNPS49JEPNK6QK928EC1HXR8" // system
        const val TAG_ID_04 = "01KNPS4KQY269H3KZD69YE5KA9" // security
        const val TAG_ID_05 = "01KNPS4X4YC6CVHHEEAJS99V39" // performance
        const val TAG_ID_06 = "01KNPS54SPM482F1V2MJVY4E8D" // testing
        const val TAG_ID_07 = "01KNPS5CD6HKVPR18G906MT0Z1" // documentation
        const val TAG_ID_08 = "01KNPS5MVETD74BD5NYCZVWY9Y" // deployment
        const val TAG_ID_09 = "01KNPS5XQ6PS3KPZD4XJY730GE" // cloud
        const val TAG_ID_10 = "01KNPS64WEQT15FSXJ0P0FD1DC" // architecture

        const val PTAG_ID_01 = TAG_ID_01 // code -> testing, documentation
        const val PTAG_ID_02 = TAG_ID_02 // database -> architecture
        const val PTAG_ID_03 = TAG_ID_03 // system -> security, performance, cloud
        const val PTAG_ID_04 = TAG_ID_09 // system -> cloud -> deployment

        val tagsWithoutParents =
            listOf(
                Triple(TAG_ID_01.toULID(), null, TAG_01),
                Triple(TAG_ID_02.toULID(), null, TAG_02),
                Triple(TAG_ID_03.toULID(), null, TAG_03),
            )

        val tagsWithParents =
            listOf(
                Triple(TAG_ID_06.toULID(), PTAG_ID_01.toULID(), TAG_06),
                Triple(TAG_ID_07.toULID(), PTAG_ID_01.toULID(), TAG_07),
                Triple(TAG_ID_10.toULID(), PTAG_ID_02.toULID(), TAG_10),
                Triple(TAG_ID_04.toULID(), PTAG_ID_03.toULID(), TAG_04),
                Triple(TAG_ID_05.toULID(), PTAG_ID_03.toULID(), TAG_05),
                Triple(TAG_ID_09.toULID(), PTAG_ID_03.toULID(), TAG_09),
                Triple(TAG_ID_08.toULID(), PTAG_ID_04.toULID(), TAG_08),
            )

        val tagIds = tagsWithoutParents.map { it.first } + tagsWithParents.map { it.first }
    }
}

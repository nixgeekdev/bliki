package dev.nixgeek.bliki.lib.test.fixtures.json.model

data class USAddress(
    val id: Int,
    val street1: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val street2: String? = null,
    val street3: String? = null,
    val type: USAddressType = USAddressType.HOME,
)

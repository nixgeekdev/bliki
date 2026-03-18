package dev.nixgeek.bliki.lib.test.fixtures.data.fakes

import java.util.concurrent.ConcurrentHashMap

interface FakeTestRepository<K, V> {
    val cache: ConcurrentHashMap<K, V>

    fun clear()
}

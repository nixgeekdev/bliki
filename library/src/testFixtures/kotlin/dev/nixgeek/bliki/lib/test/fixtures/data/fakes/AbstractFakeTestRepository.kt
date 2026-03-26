package dev.nixgeek.bliki.lib.test.fixtures.data.fakes

import ulid.ULID
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractFakeTestRepository<K : ULID, V> : FakeTestRepository<K, V> {
    override val cache: ConcurrentHashMap<K, V> = ConcurrentHashMap()

    override fun clear() {
        cache.clear()
    }

    abstract fun create(record: V): V
}

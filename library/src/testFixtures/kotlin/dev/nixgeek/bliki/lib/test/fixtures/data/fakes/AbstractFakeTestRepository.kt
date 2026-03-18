package dev.nixgeek.bliki.lib.test.fixtures.data.fakes

import java.util.concurrent.ConcurrentHashMap

abstract class AbstractFakeTestRepository<K, V> : FakeTestRepository<K, V> {
    override val cache: ConcurrentHashMap<K, V> = ConcurrentHashMap()

    override fun clear() {
        cache.clear()
    }

    abstract fun create(record: V): V
}

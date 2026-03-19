package dev.nixgeek.bliki.lib.usecase

import reactor.core.publisher.Mono

interface UseCase<Args, Res : Any> {
    operator fun invoke(args: Args): Mono<Res>
}

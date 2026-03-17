package dev.nixgeek.bliki.lib.usecase

import reactor.core.publisher.Mono

interface UnitUseCase<Res : Any> : UseCase<Unit, Res> {
    operator fun invoke(): Mono<Res>

    override operator fun invoke(args: Unit): Mono<Res> = invoke()
}

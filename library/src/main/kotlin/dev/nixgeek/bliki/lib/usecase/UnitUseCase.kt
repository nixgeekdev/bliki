package dev.nixgeek.bliki.lib.usecase

interface UnitUseCase<Res> : UseCase<Unit, Res> {
    suspend operator fun invoke(): Res

    override suspend operator fun invoke(args: Unit): Res = invoke()
}

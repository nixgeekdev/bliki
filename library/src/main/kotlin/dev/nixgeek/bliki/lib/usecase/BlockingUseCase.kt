package dev.nixgeek.bliki.lib.usecase

interface BlockingUseCase<Args, Res> {
    operator fun invoke(args: Args): Res
}

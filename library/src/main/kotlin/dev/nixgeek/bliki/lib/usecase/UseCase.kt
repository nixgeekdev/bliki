package dev.nixgeek.bliki.lib.usecase

interface UseCase<Args, Res> {
    suspend operator fun invoke(args: Args): Res
}

package deps

object Lottie : Dependency() {

    object Versions {
        const val lottie = "6.6.2"
        const val lottie_compose = "6.6.2"
    }

    private const val lottie = "com.airbnb.android:lottie:${Versions.lottie}"
    private const val composeLottie = "com.airbnb.android:lottie-compose:${Versions.lottie_compose}"


    override fun implementations() = listOf(
        lottie,
        composeLottie
    )
}
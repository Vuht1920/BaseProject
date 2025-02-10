package deps

object SplashScreen : Dependency() {
    private const val splashScreen = "androidx.core:core-splashscreen:1.0.0-beta02"
    override fun implementations() = listOf(
        splashScreen,
    )
}
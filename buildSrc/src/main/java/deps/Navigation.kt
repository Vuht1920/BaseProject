package deps

object Navigation : Dependency() {

    object Versions {
        const val navVersion = "2.8.6"
    }

    private const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navVersion}"
    private const val navigationUI = "androidx.navigation:navigation-ui-ktx:${Versions.navVersion}"


    override fun implementations() = listOf(
        navigationFragment,
        navigationUI,
    )
}
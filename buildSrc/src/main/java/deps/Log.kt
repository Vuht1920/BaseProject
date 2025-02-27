package deps

object Log : Dependency() {
    object Versions {
        const val timber = "5.0.1"
    }

    private const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    override fun implementations() = listOf(
        timber
    )
}
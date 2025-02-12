package deps

object BlankJUtil : Dependency() {

    object Versions {
        const val VERSION = "1.30.7"
    }

    private const val blankj = "com.blankj:utilcode:${Versions.VERSION}"


    override fun api() = listOf(
        blankj
    )
}
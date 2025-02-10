package deps

object Hilt : Dependency() {
    object Versions {
        const val hilt = "2.51.1"
        const val hiltWork = "1.2.0"
    }
    private const val android = "com.google.dagger:hilt-android:${Versions.hilt}"
    private const val compiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt}"
    private const val hiltWork = "androidx.hilt:hilt-work:${Versions.hiltWork}"
    private const val hiltKapt = "androidx.hilt:hilt-compiler:${Versions.hiltWork}"

    override fun implementations() = listOf(
        android,
        hiltWork
    )

    override fun kapt() = listOf(
        compiler,
        hiltKapt
    )
}
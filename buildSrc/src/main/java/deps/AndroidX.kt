package deps

object AndroidX : Dependency() {

    object Versions {
        const val CORE_KTX = "1.13.1"
        const val MATERIAL = "1.12.0"
        const val MULTIDEX = "2.0.1"
        const val ANDROIDX_COMPAT = "1.7.0"
    }

    private const val coreKtx = "androidx.core:core-ktx:${Versions.CORE_KTX}"
    private const val material = "com.google.android.material:material:${Versions.MATERIAL}"
    private const val multidex = "androidx.multidex:multidex:${Versions.MULTIDEX}"
    private const val appcompat = "androidx.multidex:multidex:${Versions.ANDROIDX_COMPAT}"


    override fun api() = listOf(
        coreKtx,
        material,
        multidex,
        appcompat
    )
}
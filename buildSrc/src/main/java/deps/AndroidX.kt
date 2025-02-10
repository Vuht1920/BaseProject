package deps

object AndroidX : Dependency() {

    object Versions {
        const val CORE_KTX = "1.15.0"
        const val MATERIAL = "1.12.0"
    }

    private const val coreKtx = "androidx.core:core-ktx:${Versions.CORE_KTX}"
    private const val material = "com.google.android.material:material:${Versions.MATERIAL}"


    override fun implementations() = listOf(
        coreKtx,
        material,
    )
}
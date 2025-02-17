package deps

object Ads : Dependency() {

    object Versions {
        const val ADMOB = "23.3.0"
        const val UMP = "3.0.0"
    }

    private const val admob = "com.google.android.gms:play-services-ads:${Versions.ADMOB}"
    private const val ump = "com.google.android.ump:user-messaging-platform:${Versions.UMP}"


    override fun api() = listOf(
        admob,
        ump
    )
}
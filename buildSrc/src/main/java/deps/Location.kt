package deps

object Location : Dependency() {
    object Versions {
        const val LOCATION = "21.3.0"
    }

    private const val location = "com.google.android.gms:play-services-location:${Versions.LOCATION}"
//    private const val geofire = "com.firebase:geofire-android-common:3.1.0"


    override fun implementations() = listOf(
       location
    )
}
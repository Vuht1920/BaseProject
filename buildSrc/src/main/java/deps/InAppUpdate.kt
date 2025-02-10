package deps

object InAppUpdate : Dependency() {
    private const val inAppUpdate = "com.google.android.play:app-update-ktx:2.0.1"
    override fun implementations() = listOf(
        inAppUpdate,
    )
}
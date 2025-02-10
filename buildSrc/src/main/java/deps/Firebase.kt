package deps

object Firebase : Dependency() {

    object Versions {
        const val firebaseBom = "33.8.0"
    }

    private const val firebaseBom = "com.google.firebase:firebase-bom:${Versions.firebaseBom}"
    private const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics"
    private const val firebaseAnalytics = "com.google.firebase:firebase-analytics"
    private const val firebaseAuth = "com.google.firebase:firebase-auth-ktx"
    private const val firebaseDynamicLink = "com.google.firebase:firebase-dynamic-links-ktx"
    private const val firebaseMessage = "com.google.firebase:firebase-messaging"
    private const val firebasePerformance = "com.google.firebase:firebase-perf"
    private const val firestore = "com.google.firebase:firebase-firestore-ktx"
    private const val firebaseRemoteConfig = "com.google.firebase:firebase-config-ktx"

    override fun implementations() = listOf(
        firebaseCrashlytics,
        firebaseAnalytics,
        firebaseRemoteConfig
    )

    override fun platformImplementations() = listOf(firebaseBom)
}
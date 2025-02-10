package deps

object InAppReview : Dependency() {
    private const val inAppReview = "com.google.android.play:review-ktx:2.0.1"

    override fun implementations() = listOf(
        inAppReview,
    )
}
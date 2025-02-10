package deps

object Billing : Dependency() {

    object Versions {
        const val BILLING = "7.1.1"
    }

    private const val billing = "com.android.billingclient:billing-ktx:${Versions.BILLING}"


    override fun implementations() = listOf(
        billing
    )
}
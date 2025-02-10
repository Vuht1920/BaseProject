package deps

object WorkManager : Dependency() {
    object Versions {
        const val WORK_MANAGER = "2.10.0"
    }
    private const val workManager = "androidx.work:work-runtime-ktx:${Versions.WORK_MANAGER}"
    override fun implementations() = listOf(
        workManager
    )
}
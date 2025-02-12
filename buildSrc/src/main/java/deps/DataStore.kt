package deps

object DataStore : Dependency() {

    object Versions {
        const val DATA_STORE = "1.1.2"
    }

    private const val dataStorePreference = "androidx.datastore:datastore-preferences:${Versions.DATA_STORE}"
    private const val dataStoreProto = "androidx.datastore:datastore:${Versions.DATA_STORE}"

    override fun api() = listOf(
        dataStorePreference,
        dataStoreProto,
    )
}
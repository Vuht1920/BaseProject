package deps

object CameraX : Dependency() {

    object Versions {
        const val CAMERA_VERSION = "1.4.1"
    }

    private const val core = "androidx.camera:camera-core:${Versions.CAMERA_VERSION}"
    private const val camera2 = "androidx.camera:camera-camera2:${Versions.CAMERA_VERSION}"
    private const val cameraLifecycle = "androidx.camera:camera-lifecycle:${Versions.CAMERA_VERSION}"
    private const val cameraVideoCapture = "androidx.camera:camera-video:${Versions.CAMERA_VERSION}"
    private const val cameraExtension = "androidx.camera:camera-extensions:${Versions.CAMERA_VERSION}"
    private const val cameraView = "androidx.camera:camera-view:${Versions.CAMERA_VERSION}"
    private const val concurrentAndroidX = "androidx.concurrent:concurrent-futures-ktx:1.1.0"

    override fun implementations() = listOf(
        core,
        camera2,
        cameraLifecycle,
        cameraVideoCapture,
        cameraExtension,
        cameraView,
        concurrentAndroidX
    )
}
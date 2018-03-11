package soutvoid.com.sudokusolver

import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by andrew on 11.03.18.
 */
class OpenCvLoadHelper {

    private var openCvLoaded = false
    private var openCvLoadInProgress = false
    private val actionsQueue: Queue<() -> Unit> = LinkedBlockingQueue()

    companion object {
        val instance: OpenCvLoadHelper by lazy { OpenCvLoadHelper() }
    }

    private fun loadOpenCv() {
        openCvLoadInProgress = true
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, App.instance, object : BaseLoaderCallback(App.instance) {
            override fun onManagerConnected(status: Int) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    openCvLoaded()
                } else {
                    super.onManagerConnected(status)
                }
            }
        })
    }

    private fun openCvLoaded() {
        openCvLoaded = true
        openCvLoadInProgress = false
        actionsQueue.forEach { invokeAction(it) }
    }

    @Synchronized
    private fun invokeAction(action: () -> Unit) {
        action()
    }

    fun unloadOpenCv() {
        openCvLoaded = false
        openCvLoadInProgress = false
    }

    fun doUsingOpenCv(action: () -> Unit) {
        if (openCvLoaded)
            action()
        else {
            actionsQueue.add(action)
            if (!openCvLoadInProgress)
                loadOpenCv()
        }
    }

}

fun doUsingOpenCv(action: () -> Unit) {
    OpenCvLoadHelper.instance.doUsingOpenCv(action)
}

fun unloadOpenCv() {
    OpenCvLoadHelper.instance.unloadOpenCv()
}
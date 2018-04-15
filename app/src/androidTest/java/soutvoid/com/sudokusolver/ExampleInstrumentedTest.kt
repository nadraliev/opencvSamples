package soutvoid.com.sudokusolver

import android.support.annotation.DrawableRes
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("soutvoid.com.opencvsamples", appContext.packageName)
    }

    @Test
    fun testDigitRecognition() {
        doUsingOpenCv {
            val recognizer = DigitRecognizer()
            recognizer.train("train-images-idx3-ubyte", "train-labels-idx1-ubyte", InstrumentationRegistry.getTargetContext())
            assertEquals(1, recognizer.classify(loadImage(R.drawable.one_hand, recognizer.numCols, recognizer.numRows)))
            assertEquals(3, recognizer.classify(loadImage(R.drawable.three_hand, recognizer.numCols, recognizer.numRows)))
        }
    }

    fun loadImage(@DrawableRes id: Int, numCols: Int, numRows: Int): Mat {
        val img = Utils.loadResource(InstrumentationRegistry.getTargetContext(), id)
        Imgproc.resize(img, img, Size(numCols.toDouble(), numRows.toDouble()))
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY)
        val test = Mat(1, img.rows() * img.cols(), CvType.CV_32F)
        var count = 0
        for (i in 0 until img.rows()) {
            for (j in 0 until img.cols()) {
                test.put(0, count, img.get(i, j)[0])
                count++
            }
        }
        return test
    }
}

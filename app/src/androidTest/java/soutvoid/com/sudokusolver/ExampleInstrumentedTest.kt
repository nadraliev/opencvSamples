package soutvoid.com.sudokusolver

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat

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
            val bitmaps = mutableListOf<Bitmap>()
            recognizer.train(InstrumentationRegistry.getTargetContext(), bitmaps)
            val folderFormat = "Sample0%02d"
            val stringBuilder = StringBuilder()
            repeat(10) { folderIndex ->
                stringBuilder.append("\nProcessing digit $folderIndex\n")
                InstrumentationRegistry.getTargetContext().resources.assets.list(folderFormat.format(folderIndex + 1)).take(30).forEachIndexed { fileIndex, fileName ->
                    val path = "${folderFormat.format(folderIndex + 1)}/$fileName"
                    stringBuilder.append("\nProcessing $path\n")
                    val img = Mat(128, 128, CvType.CV_8UC1)
                    Utils.bitmapToMat(BitmapFactory.decodeStream(InstrumentationRegistry.getTargetContext().assets.open(path)), img)
                    Core.bitwise_not(img, img)
                    stringBuilder.append(recognizer.classify(img, bitmaps))
                }
            }
            println(stringBuilder.toString())
        }
    }
}

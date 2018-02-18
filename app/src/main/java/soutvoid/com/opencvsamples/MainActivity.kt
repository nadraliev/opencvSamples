package soutvoid.com.opencvsamples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initOpenCv {
            val img = Utils.loadResource(this, R.drawable.sudoku_jpg)
            Imgproc.GaussianBlur(img, img, Size(3.0, 3.0), .0)
            Imgproc.adaptiveThreshold(img, img, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 10.0)
            Core.bitwise_not(img, img)

            val lines = Mat()
            println(img.width())
            Imgproc.HoughLinesP(img, lines, 1.0, Math.PI/180, 100, 400.0, 100.0)

            println("cols: ${lines.cols()}")
            println("lines: ${lines.rows()}")

            Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2RGB)

            (1..lines.rows()).mapNotNull { lines.get(it, 0) }
                    .forEach {
                        Imgproc.line(img, Point(it[0], it[1]), Point(it[2], it[3]), Scalar(255.0, .0, .0), 3)
                    }

            imageView.setImageBitmap(img.toBitmap())
        }
    }

    private fun initOpenCv(onSuccess: () -> Unit) {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, object: BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    onSuccess()
                } else {
                    super.onManagerConnected(status)
                }
            }
        })
    }


}

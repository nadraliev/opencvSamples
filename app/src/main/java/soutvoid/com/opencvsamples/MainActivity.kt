package soutvoid.com.opencvsamples

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat

import kotlinx.android.synthetic.main.activity_main.view.*
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, this, object: BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    var bitmap = BitmapFactory.decodeResource(resources, R.drawable.sudoku)
                    val mat = Mat()
                    Utils.bitmapToMat(bitmap, mat)
                    Imgproc.GaussianBlur(mat, mat, Size(101.0, 101.0), 23.43)
                    imageView.setImageBitmap(Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888).apply { Utils.matToBitmap(mat, this) })
                } else {
                    super.onManagerConnected(status)
                }
            }
        })
    }
}

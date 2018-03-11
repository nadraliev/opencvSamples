package soutvoid.com.sudokusolver

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadImageBtn.onClick {
            val getIntent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            val chooser = Intent.createChooser(getIntent, "Select picture").apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
            }
            startActivityForResult(chooser, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                val inputStream = baseContext.contentResolver.openInputStream(it)
                doUsingOpenCv {
                    val bmp = BitmapFactory.decodeStream(inputStream)
                    bmp ?: return@doUsingOpenCv
                    val img = Mat().apply { Utils.bitmapToMat(bmp, this) }
                    Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY)
                    Imgproc.GaussianBlur(img, img, Size(3.0, 3.0), .0)
                    Imgproc.adaptiveThreshold(img, img, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 10.0)
                    Core.bitwise_not(img, img)

                    val lines = Mat()
                    println(img.width())
                    Imgproc.HoughLinesP(img, lines, 1.0, Math.PI / 180, 80, img.width() / 2.0, img.width() / 20.0)

                    println("cols: ${lines.cols()}")
                    println("lines: ${lines.rows()}")

                    Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2RGB)

                    (1..lines.rows()).mapNotNull { lines.get(it, 0) }
                            .filter {
                                Math.sqrt(Math.pow(it[0] - it[2], 2.0) + Math.pow(it[1] - it[3], 2.0)) >= img.width() * 0.8
                            }.also { println("lines filtered: ${it.count()}") }
                            .forEach {
                                Imgproc.line(img, Point(it[0], it[1]), Point(it[2], it[3]), Scalar((Math.random() * 1000) % 250, (Math.random() * 1000) % 250, (Math.random() * 1000) % 250), 3)
                            }



                    imageView.setImageBitmap(img.toBitmap())
                }
            }
        }
    }

    override fun onPause() {
        unloadOpenCv()
        super.onPause()
    }

}

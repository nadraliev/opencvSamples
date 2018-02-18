package soutvoid.com.opencvsamples

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat

fun Mat.toBitmap(): Bitmap =
        Bitmap.createBitmap(width(), height(), Bitmap.Config.ARGB_4444)
                .also { Utils.matToBitmap(this, it) }
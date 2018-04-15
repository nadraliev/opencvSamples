package soutvoid.com.sudokusolver

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(2018915346, byteArrayOf(0x12, 0x34, 0x56, 0x78).reversedArray().toInt())
    }
}

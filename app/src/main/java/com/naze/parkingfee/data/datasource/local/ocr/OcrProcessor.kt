package com.naze.parkingfee.data.datasource.local.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

/**
 * Wrapper around Google ML Kit Text Recognition (Korean).
 * Provides helper functions to run OCR from URIs or Bitmaps.
 */
class OcrProcessor @Inject constructor(
    private val context: Context
) {

    @Volatile
    private var recognizer: com.google.mlkit.vision.text.TextRecognizer? = null
    private val lock = Any()

    /**
     * Recognizer를 가져옵니다. 이미 close된 경우 새로 생성합니다.
     */
    private fun getRecognizer(): com.google.mlkit.vision.text.TextRecognizer {
        return recognizer ?: synchronized(lock) {
            recognizer ?: TextRecognition.getClient(
                KoreanTextRecognizerOptions.Builder().build()
            ).also { recognizer = it }
        }
    }

    /**
     * Runs text recognition on an image referenced by [imageUri].
     */
    suspend fun recognizeTextFromUri(imageUri: Uri): OcrResult {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = getRecognizer().process(image).await()
            toOcrResult(result.text, result.textBlocks.map { block ->
                TextBlock(
                    text = block.text,
                    lines = block.lines.map { line -> line.text },
                    boundingBox = block.boundingBox
                )
            })
        } catch (e: IOException) {
            OcrResult(
                fullText = "",
                textBlocks = emptyList(),
                isSuccess = false,
                errorMessage = "Failed to load image: ${e.message}"
            )
        } catch (e: Exception) {
            OcrResult(
                fullText = "",
                textBlocks = emptyList(),
                isSuccess = false,
                errorMessage = "Text recognition failed: ${e.message}"
            )
        }
    }

    /**
     * Runs text recognition directly on a [Bitmap].
     */
    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): OcrResult {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = getRecognizer().process(image).await()
            toOcrResult(result.text, result.textBlocks.map { block ->
                TextBlock(
                    text = block.text,
                    lines = block.lines.map { line -> line.text },
                    boundingBox = block.boundingBox
                )
            })
        } catch (e: Exception) {
            OcrResult(
                fullText = "",
                textBlocks = emptyList(),
                isSuccess = false,
                errorMessage = "Text recognition failed: ${e.message}"
            )
        }
    }

    /**
     * Parses recognized text to guess parking lot information.
     * This is a best-effort heuristic and can be improved later.
     */
    fun parseParkingLotInfo(ocrResult: OcrResult): ParsedParkingInfo {
        val parkingLotKeyword = "\uC8FC\uCC28" // "주차"
        val publicParkingKeyword = "\uACF5\uC601\uC8FC\uCC28" // "공영주차"
        val privateParkingKeyword = "\uBBFC\uC601\uC8FC\uCC28" // "민영주차"
        val wonKeyword = "\uC6D0" // "원"
        val minuteKeyword = "\uBD84" // "분"
        val hourKeyword = "\uC2DC\uAC04" // "시간"

        val parkingLotName = ocrResult.textBlocks.firstOrNull { block ->
            block.text.contains(parkingLotKeyword, ignoreCase = true) ||
                block.text.contains(publicParkingKeyword, ignoreCase = true) ||
                block.text.contains(privateParkingKeyword, ignoreCase = true) ||
                block.text.contains("PARKING", ignoreCase = true)
        }?.text

        val feeInfo = ocrResult.textBlocks.firstOrNull { block ->
            block.text.contains(wonKeyword, ignoreCase = true) &&
                (block.text.contains(minuteKeyword, ignoreCase = true) ||
                    block.text.contains(hourKeyword, ignoreCase = true))
        }?.text

        return ParsedParkingInfo(
            parkingLotName = parkingLotName,
            feeInfo = feeInfo
        )
    }

    fun close() {
        synchronized(lock) {
            recognizer?.close()
            recognizer = null
        }
    }

    private fun toOcrResult(
        fullText: String,
        textBlocks: List<TextBlock>
    ): OcrResult = OcrResult(
        fullText = fullText,
        textBlocks = textBlocks,
        isSuccess = true,
        errorMessage = null
    )

    data class OcrResult(
        val fullText: String,
        val textBlocks: List<TextBlock>,
        val isSuccess: Boolean,
        val errorMessage: String?
    )

    data class TextBlock(
        val text: String,
        val lines: List<String>,
        val boundingBox: android.graphics.Rect?
    )

    data class ParsedParkingInfo(
        val parkingLotName: String?,
        val feeInfo: String?
    )
}

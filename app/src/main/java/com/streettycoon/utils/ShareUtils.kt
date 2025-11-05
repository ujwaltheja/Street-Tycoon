package com.streettycoon.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Utility for creating and sharing milestone cards
 */
object ShareUtils {

    /**
     * Create a milestone achievement card image
     */
    fun createMilestoneCard(
        context: Context,
        title: String,
        subtitle: String,
        stats: List<Pair<String, String>>
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background gradient
        canvas.drawColor(Color.parseColor("#FF6B35"))

        // Title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 120f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(title, width / 2f, 300f, titlePaint)

        // Subtitle
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#FFF8E1")
            textSize = 60f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(subtitle, width / 2f, 400f, subtitlePaint)

        // Stats
        val statPaint = Paint().apply {
            color = Color.WHITE
            textSize = 80f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val statLabelPaint = Paint().apply {
            color = Color.parseColor("#FFF8E1")
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        var yPosition = 700f
        stats.forEach { (label, value) ->
            canvas.drawText(value, width / 2f, yPosition, statPaint)
            canvas.drawText(label, width / 2f, yPosition + 80, statLabelPaint)
            yPosition += 250f
        }

        // Footer
        val footerPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Street Tycoon", width / 2f, height - 150f, footerPaint)

        return bitmap
    }

    /**
     * Share milestone card
     */
    fun shareMilestoneCard(
        context: Context,
        cash: Double,
        customersServed: Long,
        day: Int
    ) {
        val stats = listOf(
            "Total Cash" to "₹${formatLargeNumber(cash)}",
            "Customers Served" to formatLargeNumber(customersServed.toDouble()),
            "Day" to "$day"
        )

        val bitmap = createMilestoneCard(
            context,
            "Achievement Unlocked!",
            "Street Tycoon Progress",
            stats
        )

        // Save bitmap to cache
        val file = File(context.cacheDir, "milestone_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Share
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Check out my Street Tycoon progress! I've earned ₹${formatLargeNumber(cash)} and served $customersServed customers!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share your achievement"))
    }

    private fun formatLargeNumber(value: Double): String {
        return when {
            value >= 1_000_000_000 -> String.format("%.2fB", value / 1_000_000_000)
            value >= 1_000_000 -> String.format("%.2fM", value / 1_000_000)
            value >= 1_000 -> String.format("%.2fK", value / 1_000)
            else -> String.format("%.0f", value)
        }
    }
}

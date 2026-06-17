package com.example.plantscienceapp.adapter

import android.content.Context
import android.content.ContextWrapper
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.plantscienceapp.R
import com.example.plantscienceapp.data.entity.Plant
import com.example.plantscienceapp.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlantAdapter(
    private val onPlantClick: (Plant) -> Unit,
    private val onWaterClick: ((Plant) -> Unit)? = null
) : ListAdapter<Plant, PlantAdapter.PlantViewHolder>(PlantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant_card, parent, false)
        return PlantViewHolder(view, onPlantClick, onWaterClick)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlantViewHolder(
        itemView: View,
        private val onPlantClick: (Plant) -> Unit,
        private val onWaterClick: ((Plant) -> Unit)? = null
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivPlantThumb: ImageView = itemView.findViewById(R.id.ivPlantThumb)
        private val tvPlantName: TextView = itemView.findViewById(R.id.tvPlantName)
        private val tvScientificName: TextView = itemView.findViewById(R.id.tvScientificName)

        private val layoutWatering: View = itemView.findViewById(R.id.layoutWatering)
        private val tvWateringStatus: TextView = itemView.findViewById(R.id.tvWateringStatus)
        private val btnWater: View = itemView.findViewById(R.id.btnWater)

        private var currentPlantId: Long? = null
        private var loadJob: Job? = null

        fun bind(plant: Plant) {
            loadJob?.cancel()
            currentPlantId = plant.plantId
            ivPlantThumb.setImageBitmap(null)
            ivPlantThumb.setImageResource(R.drawable.ic_launcher_background)

            val context = itemView.context

            tvPlantName.text =
                if (plant.isCollected) plant.name else context.getString(R.string.unknown_plant)
            tvScientificName.text = plant.scientificName

            if (plant.isCollected) {
                ivPlantThumb.clearColorFilter()
            } else {
                val matrix = ColorMatrix().apply { setSaturation(0f) }
                ivPlantThumb.colorFilter = ColorMatrixColorFilter(matrix)
            }

            if (plant.isMyPlanting) {
                layoutWatering.visibility = View.VISIBLE
                val nextWaterTime =
                    plant.lastWateredTime + (plant.wateringFrequency * 24L * 60 * 60 * 1000)
                val timeLeft = nextWaterTime - System.currentTimeMillis()
                val totalHoursLeft = timeLeft / (60 * 60 * 1000)

                when {
                    totalHoursLeft >= 24 -> {
                        val days = (totalHoursLeft / 24).toInt()
                        tvWateringStatus.text = context.getString(R.string.watering_days_left, days)
                        tvWateringStatus.setTextColor(0xFF2E7D32.toInt())
                    }

                    totalHoursLeft >= 0 -> {
                        tvWateringStatus.text = context.getString(R.string.watering_today)
                        tvWateringStatus.setTextColor(0xFFFF9800.toInt())
                    }

                    else -> {
                        val daysOverdue = Math.abs(totalHoursLeft / 24).toInt() + 1
                        tvWateringStatus.text =
                            context.getString(R.string.watering_overdue, daysOverdue)
                        tvWateringStatus.setTextColor(0xFFD32F2F.toInt())
                    }
                }
                btnWater.setOnClickListener { onWaterClick?.invoke(plant) }
            } else {
                layoutWatering.visibility = View.GONE
            }

            val imageSource = plant.imageName
            if (imageSource.isNotEmpty()) {
                val file = if (imageSource.startsWith("/")) File(imageSource)
                else File(context.filesDir, imageSource)

                if (file.exists()) {
                    val lifecycleOwner =
                        itemView.findViewTreeLifecycleOwner() ?: context.findLifecycleOwner()
                    loadJob = lifecycleOwner?.lifecycleScope?.launch {
                        val bitmap = withContext(Dispatchers.IO) {
                            ImageUtils.decodeSampledBitmapFromFile(file.absolutePath, 300, 300)
                        }
                        if (isActive && currentPlantId == plant.plantId && bitmap != null) {
                            ivPlantThumb.setImageBitmap(bitmap)
                        }
                    }
                } else {
                    val resId = context.resources.getIdentifier(
                        imageSource,
                        "drawable",
                        context.packageName
                    )
                    if (resId != 0) ivPlantThumb.setImageResource(resId)
                }
            }

            itemView.setOnClickListener { onPlantClick(plant) }
        }

        private fun Context.findLifecycleOwner(): LifecycleOwner? {
            var curContext = this
            while (curContext is ContextWrapper) {
                if (curContext is LifecycleOwner) return curContext
                curContext = curContext.baseContext
            }
            return null
        }
    }

    class PlantDiffCallback : DiffUtil.ItemCallback<Plant>() {
        override fun areItemsTheSame(oldItem: Plant, newItem: Plant): Boolean =
            oldItem.plantId == newItem.plantId

        override fun areContentsTheSame(oldItem: Plant, newItem: Plant): Boolean =
            oldItem == newItem
    }
}

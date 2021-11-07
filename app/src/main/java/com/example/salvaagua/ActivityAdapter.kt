package com.example.salvaagua

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.salvaagua.data.entities.WaterUseLog

class ActivityAdapter(private val dataSet: List<ActivityItem>):
    RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val activityNameTxt : TextView = view.findViewById(R.id.activity_name_txt)
        val activityLitersTxt: TextView = view.findViewById(R.id.activity_liters_txt)
        val useLevelTxt : TextView = view.findViewById(R.id.use_level_txt)
        val adviceTxt : TextView = view.findViewById(R.id.advice_txt)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.activityNameTxt.text = "${dataSet[position].activityName}: "
        holder.activityLitersTxt.text = dataSet[position].activityLiters.toString()
        holder.useLevelTxt.text = dataSet[position].useLevel
        if(dataSet[position].useLevel == "Sobre Consumo"){
            holder.useLevelTxt.setTextColor(Color.RED)
        }
        else{
            holder.useLevelTxt.setTextColor(Color.GREEN)
        }
        holder.adviceTxt.isVisible = false
        if(dataSet[position].advice != ""){
            holder.adviceTxt.isVisible = true
            holder.adviceTxt.text = dataSet[position].advice
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

}
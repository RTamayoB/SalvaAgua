package com.example.salvaagua

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.core.cartesian.series.Column
import com.anychart.data.Set
import com.anychart.enums.ScaleStackMode
import com.example.salvaagua.databinding.FragmentWeeklyChartBinding
import com.example.salvaagua.viewmodels.ChartsViewModel
import com.example.salvaagua.viewmodels.ChartsViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WeeklyChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WeeklyChartFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentWeeklyChartBinding? = null
    private val binding get() = _binding!!

    lateinit var weeklySet : Set

    private val chartsViewModel: ChartsViewModel by viewModels {
        ChartsViewModelFactory((requireActivity().application as MyApplication).waterUseLogRepository)
    }

    lateinit var housePreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        housePreferences = requireActivity().getSharedPreferences("house", AppCompatActivity.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWeeklyChartBinding.inflate(inflater, container, false)
        weeklySet = Set.instantiate()
        binding.weeklyChart.setProgressBar(binding.weeklyChartProgressBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val date = Calendar.getInstance()
        val date1 = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        date.add(Calendar.DATE, -3)
        val startDate = date.time
        date1.add(Calendar.DATE,3)
        val endDate = date1.time
        val s = sdf.format(startDate)
        val e = sdf.format(endDate)

        val cartesian = AnyChart.column()
        cartesian.animation(true)
        cartesian.noData().label("No hay datos \\n\\n " +
                "¡Comienze a registrar su uso \\n\\n" +
                "agregando una actividad!")
        cartesian.title("$s - $e")
        cartesian.yScale().stackMode(ScaleStackMode.VALUE)
        val dayLimit = (housePreferences.getFloat("goal_percentage", 0.0F)) / 1000
        val line = cartesian.annotations()
        line.horizontalLine("{ valueAnchor: $dayLimit }")

        val weekData = arrayListOf<DataEntry>()
        chartsViewModel.waterUseLogByWeek(startDate, endDate)
            .observe(requireActivity()) { weekLogs ->
                val logMap = LinkedHashMap<String,LinkedHashMap<String,Float>>()
                Log.d("Week Size", weekLogs.size.toString())
                for(i in weekLogs.indices){
                    val log = weekLogs[i]

                    if(logMap[log.day].isNullOrEmpty()){
                        logMap[log.day] = linkedMapOf()
                    }

                    if(logMap[log.day]?.get(log.activity) == null){
                        logMap[log.day]!![log.activity] = log.waterUsed/1000
                    }
                    else{
                        logMap[log.day]?.get(log.activity)?.plus(log.waterUsed/1000)
                    }
                }

                for((day, map) in logMap){
                    Log.d("Md", day)
                    weekData.add(
                        MonthDataEntry(
                            day,
                            map["Bañarse"],
                            map["Usar el retrete"],
                            map["Lavarse las manos"],
                            map["Cepillarse los dientes"],
                            map["Afeitarse"],
                            map["Lavar los trastes"],
                            map["Lavar el coche"],
                            map["Regar plantas"]
                        )
                    )
                }
                Log.d("WeekData", weekData.toString())
                weeklySet.data(weekData)
                val showerData = weeklySet.mapAs("{ x: 'x', value: 'shower' }")
                val wcData = weeklySet.mapAs("{ x: 'x', value: 'wc' }")
                val handsData = weeklySet.mapAs("{ x: 'x', value: 'hands' }")
                val brushData = weeklySet.mapAs("{ x: 'x', value: 'brush' }")
                val shaveData = weeklySet.mapAs("{ x: 'x', value: 'shave' }")
                val dishesData = weeklySet.mapAs("{ x: 'x', value: 'dishes' }")
                val carData = weeklySet.mapAs("{ x: 'x', value: 'car' }")
                val plantsData = weeklySet.mapAs("{ x: 'x', value: 'plants' }")

                var series = cartesian.column(showerData)
                setupSeriesLabels(series, "Bañarse")

                series = cartesian.column(wcData)
                setupSeriesLabels(series, "Usar el retrete")

                series = cartesian.column(handsData)
                setupSeriesLabels(series, "Lavarse las manos")

                series = cartesian.column(brushData)
                setupSeriesLabels(series, "Cepillarse los dientes")

                series = cartesian.column(shaveData)
                setupSeriesLabels(series, "Afeitarse")

                series = cartesian.column(dishesData)
                setupSeriesLabels(series, "Lavar los trastes")

                series = cartesian.column(carData)
                setupSeriesLabels(series, "Lavar el coche")

                series = cartesian.column(plantsData)
                setupSeriesLabels(series, "Regar plantas")

                val xAxis = cartesian.xAxis(0)
                xAxis.title("Meses")

                val yAxis = cartesian.yAxis(0)
                yAxis.title("Metros Cúbicos")
            }
        binding.weeklyChart.setChart(cartesian)
    }

    private fun setupSeriesLabels(series: Column, name: String){
        series.name(name)
    }

    private class MonthDataEntry constructor(
        x: String?,
        shower: Number?,
        wc: Number?,
        hands: Number?,
        brush: Number?,
        shave: Number?,
        dishes: Number?,
        car: Number?,
        plants: Number?
    ) :
        ValueDataEntry(x, shower) {
        init {
            setValue("wc", wc)
            setValue("hands", hands)
            setValue("brush", brush)
            setValue("shave", shave)
            setValue("dishes", dishes)
            setValue("car", car)
            setValue("plants", plants)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WeeklyChartFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeeklyChartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
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
import com.anychart.core.cartesian.series.Base
import com.anychart.core.cartesian.series.Column
import com.anychart.data.Set
import com.anychart.enums.ScaleStackMode
import com.example.salvaagua.databinding.FragmentYearlyChartBinding
import com.example.salvaagua.viewmodels.ChartsViewModel
import com.example.salvaagua.viewmodels.ChartsViewModelFactory
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.log

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [YearlyChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class YearlyChartFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentYearlyChartBinding? = null
    private val binding get() = _binding!!

    lateinit var yearlySet : Set

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
        _binding = FragmentYearlyChartBinding.inflate(inflater, container, false)
        yearlySet = Set.instantiate()
        binding.yearlyChart.setProgressBar(binding.yearlyChartProgressBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Get data for each month, stack it
        val date = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMMM", Locale.getDefault())

        val cartesian = AnyChart.column()
        cartesian.animation("true")
        cartesian.noData().label("No hay datos \\n\\n " +
                "¡Comienze a registrar su uso \\n\\n" +
                "agregando una actividad!")
        cartesian.title("Consumo de ${date.get(Calendar.YEAR)}")
        cartesian.yScale().stackMode(ScaleStackMode.VALUE)
        val monthLimit = (housePreferences.getFloat("goal_percentage", 0.0F) * 30) / 1000
        val line = cartesian.annotations()
        line.horizontalLine("{ valueAnchor: $monthLimit }")


        val monthData = arrayListOf<DataEntry>()
        chartsViewModel.waterUseLogByYear(
            date.get(Calendar.YEAR).toString())
            .observe(requireActivity()) { monthLogs ->
                //Sum each activity and stacked them
                val logMap = HashMap<String,HashMap<String,Float>>()
                Log.d("Logs", monthLogs.size.toString())
                for(j in monthLogs.indices){
                    val log = monthLogs[j]
                    val monthAsDate = Calendar.getInstance()
                    monthAsDate.set(Calendar.MONTH, log.month.toInt()-1)
                    val month = sdf.format(monthAsDate.time)

                    //if month doesnt exist, create it
                    //if activity in month, plus, else add
                    if(logMap[month.toString()].isNullOrEmpty()){
                        logMap[month.toString()] = hashMapOf()
                    }

                    if(logMap[month.toString()]?.get(log.activity) == null){
                        logMap[month.toString()]!![log.activity] = log.waterUsed/1000
                    }
                    else{
                        logMap[month.toString()]?.get(log.activity)?.plus(log.waterUsed/1000)
                    }


                }
                for(i in 0 until 12) {
                    val monthAsDate = Calendar.getInstance()
                    monthAsDate.set(Calendar.MONTH, i)
                    val month = sdf.format(monthAsDate.time)
                    Log.d("MonthIN", month)
                    if(!logMap[month].isNullOrEmpty()){
                        monthData.add(
                            MonthDataEntry(
                                month,
                                logMap[month]?.get("Bañarse"),
                                logMap[month]?.get("Usar el retrete"),
                                logMap[month]?.get("Lavarse las manos"),
                                logMap[month]?.get("Cepillarse los dientes"),
                                logMap[month]?.get("Afeitarse"),
                                logMap[month]?.get("Lavar los trastes"),
                                logMap[month]?.get("Lavar el coche"),
                                logMap[month]?.get("Regar plantas")
                            )
                        )
                    }
                }

                yearlySet.data(monthData)
                val showerData = yearlySet.mapAs("{ x: 'x', value: 'shower' }")
                val wcData = yearlySet.mapAs("{ x: 'x', value: 'wc' }")
                val handsData = yearlySet.mapAs("{ x: 'x', value: 'hands' }")
                val brushData = yearlySet.mapAs("{ x: 'x', value: 'brush' }")
                val shaveData = yearlySet.mapAs("{ x: 'x', value: 'shave' }")
                val dishesData = yearlySet.mapAs("{ x: 'x', value: 'dishes' }")
                val carData = yearlySet.mapAs("{ x: 'x', value: 'car' }")
                val plantsData = yearlySet.mapAs("{ x: 'x', value: 'plants' }")

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
        binding.yearlyChart.setChart(cartesian)
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
         * @return A new instance of fragment YearlyChartFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            YearlyChartFragment().apply {
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
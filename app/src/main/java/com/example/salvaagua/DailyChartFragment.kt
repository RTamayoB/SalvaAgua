package com.example.salvaagua

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.data.Set
import com.example.salvaagua.databinding.FragmentDailyChartBinding
import com.example.salvaagua.viewmodels.ChartsViewModel
import com.example.salvaagua.viewmodels.ChartsViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DailyChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DailyChartFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentDailyChartBinding? = null
    private val binding get() = _binding!!

    //Data sets
    lateinit var dailySet : Set
    lateinit var pie: Pie

    private val chartsViewModel: ChartsViewModel by viewModels {
        ChartsViewModelFactory((requireActivity().application as MyApplication).waterUseLogRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("State", "OnCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentDailyChartBinding.inflate(inflater,container,false)
        binding.dailyChart.setProgressBar(binding.dailyChartProgressBar)
        dailySet = Set.instantiate()

        pie = AnyChart.pie()
        pie.animation(true)
        pie.noData().label("No hay datos \\n\\n " +
                "¡Comienze a registrar su uso \\n\\n" +
                "agregando una actividad!")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("State", "OnViewCreated")
//Depending on sensorPreferences set the pie chart
        //Values to sum and put in pie chart
        //Get data from current day and put in a pieChart

        val date = Calendar.getInstance()
        val titleFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        pie.title(titleFormat.format(date.time))
        chartsViewModel.waterUseLogByDate(date.time)
            .observe(requireActivity()) { logs ->
                if(logs.isNotEmpty()) {
                    //Fill pie
                    var shower = 0.0
                    var wc = 0.0
                    var hands = 0.0
                    var brush = 0.0
                    var shave = 0.0
                    var dishes = 0.0
                    var car = 0.0
                    var plants = 0.0
                    Log.d("Qty", logs.size.toString())
                    for (i in logs.indices) {
                        val log = logs[i]
                        when (log.activity) {
                            "Bañarse" -> shower += log.waterUsed
                            "Usar el retrete" -> wc += log.waterUsed
                            "Lavarse las manos" -> hands += log.waterUsed
                            "Cepillarse los dientes" -> brush += log.waterUsed
                            "Afeitarse" -> shave += log.waterUsed
                            "Lavar los trastes" -> dishes += log.waterUsed
                            "Lavar el coche" -> car += log.waterUsed
                            "Regar plantas" -> plants += log.waterUsed
                            else -> {
                                //Do nothing
                            }
                        }
                    }
                    val dailyData = arrayListOf<DataEntry>()
                    dailyData.add(ValueDataEntry("Bañarse", shower / 1000))
                    dailyData.add(ValueDataEntry("Usar el retrete", wc / 1000))
                    dailyData.add(ValueDataEntry("Lavarse las manos", hands / 1000))
                    dailyData.add(ValueDataEntry("Cepillarse los dientes", brush / 1000))
                    dailyData.add(ValueDataEntry("Afeitarse", shave / 1000))
                    dailyData.add(ValueDataEntry("Lavar los trastes", dishes / 1000))
                    dailyData.add(ValueDataEntry("Lavar el coche", car / 1000))
                    dailyData.add(ValueDataEntry("Regar plantas", plants / 1000))
                    pie.container("container")
                    dailySet.data(dailyData)
                    val daylySeriesData = dailySet.mapAs("{ x: 'x', value: 'value' }")
                    pie.data(daylySeriesData)
                }
            }
        binding.dailyChart.setChart(pie)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DailyChartFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DailyChartFragment().apply {
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
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
import com.example.salvaagua.databinding.FragmentMonthlyChartBinding
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
 * Use the [MonthlyChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MonthlyChartFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentMonthlyChartBinding? = null
    private val binding get() = _binding!!

    private val chartsViewModel: ChartsViewModel by viewModels {
        ChartsViewModelFactory((requireActivity().application as MyApplication).waterUseLogRepository)
    }

    lateinit var monthlySet : Set
    lateinit var pie: Pie

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
        // Inflate the layout for this fragment
        _binding = FragmentMonthlyChartBinding.inflate(inflater, container, false)
        binding.monthlyChart.setProgressBar(binding.monthlyChartProgressBar)
        monthlySet = Set.instantiate()

        pie = AnyChart.pie()
        pie.animation(true)
        pie.noData().label("No hay datos \\n\\n " +
                "¡Comienze a registrar su uso \\n\\n" +
                "agregando una actividad!")

        val date = Calendar.getInstance()
        val titleFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val unformatTitle = titleFormat.format(date.time).toString()
        val title = unformatTitle.substring(0,1).uppercase() + unformatTitle.substring(1).lowercase()
        pie.title(title)
        Log.d("Month Chart", date.get(Calendar.MONTH).toString())
        chartsViewModel.waterUseLogByMonth(
            (date.get(Calendar.MONTH)+1).toString(),
            date.get(Calendar.YEAR).toString()
        ).observe(requireActivity()) { monthLogs ->
            if(monthLogs.isNotEmpty()){
                var shower = 0.0
                var wc = 0.0
                var hands = 0.0
                var brush = 0.0
                var shave = 0.0
                var dishes = 0.0
                var car = 0.0
                var plants = 0.0
                Log.d("Month Chart Qty", monthLogs.size.toString())
                for (i in monthLogs.indices) {
                    val log = monthLogs[i]
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
                monthlySet.data(dailyData)
                val daylySeriesData = monthlySet.mapAs("{ x: 'x', value: 'value' }")
                pie.data(daylySeriesData)
            }
        }
        binding.monthlyChart.setChart(pie)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Get the data for the week (current day + six days) and plot it in a stacked bar chart
        //with a line

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MonthlyChartFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MonthlyChartFragment().apply {
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
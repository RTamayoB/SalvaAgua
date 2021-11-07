package com.example.salvaagua

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.salvaagua.databinding.FragmentRegisterActivityBinding
import java.text.SimpleDateFormat
import java.util.*
import com.example.salvaagua.data.entities.WaterUseLog
import com.example.salvaagua.util.WaterUseData
import com.example.salvaagua.viewmodels.RegisterActivityViewModel
import com.example.salvaagua.viewmodels.RegisterActivityViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterActivityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterActivityFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentRegisterActivityBinding? = null
    private val binding get() = _binding!!

    lateinit var database: FirebaseFirestore
    lateinit var userPreferences: SharedPreferences

    private val registerActivityViewModel: RegisterActivityViewModel by viewModels {
        RegisterActivityViewModelFactory((requireActivity().application as MyApplication).waterUseLogRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        userPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        database = FirebaseFirestore.getInstance()
        requireActivity().title = "Registrar Actividad"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterActivityBinding.inflate(inflater, container, false)
        binding.saveActivityProgressBar.isVisible = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activityTypeArray = arrayListOf<String>()
        activityTypeArray.add("Bañarse")
        activityTypeArray.add("Usar el retrete")
        activityTypeArray.add("Lavarse las manos")
        activityTypeArray.add("Cepillarse los dientes")
        activityTypeArray.add("Afeitarse")
        activityTypeArray.add("Lavar los trastes")
        activityTypeArray.add("Lavar el coche")
        activityTypeArray.add("Regar plantas")

        val activityTypeAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, activityTypeArray)
        activityTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.activityTypeSpinner.setAdapter(activityTypeAdapter)

        binding.activityDateEdt.setOnClickListener {
            showDatePickerDialog(it)
        }

        binding.registerActivityBtn.setOnClickListener {
            if(binding.activityDateEdt.text.isNullOrEmpty()
                || binding.activityMinutesEdt.text.isNullOrEmpty()){
                Toast.makeText(requireActivity(),"Llene todos los datos",Toast.LENGTH_SHORT).show()
            }
            else {

                saveActivity()
            }
        }

        binding.activityTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selected = p0!!.getItemAtPosition(p2)
                if(selected.toString() == "Usar el retrete"){
                    binding.activityMinutesEdt.setText("1")
                    binding.activityMinutesInputLayout.visibility = View.GONE
                    binding.activityMinutesEdt.visibility = View.GONE
                }
                else{
                    binding.activityMinutesEdt.setText("")
                    binding.activityMinutesInputLayout.visibility = View.VISIBLE
                    binding.activityMinutesEdt.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //
            }

        }
    }

    fun saveActivity(){

        val useData = WaterUseData()

        binding.saveActivityProgressBar.isVisible = true

        //Parse Date to get separate Day, Month and Year values
        val date = binding.activityDateEdt.text.toString()
        val dateConverter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatDay = SimpleDateFormat("dd", Locale.US)
        val formatMonth = SimpleDateFormat("MM", Locale.US)
        val formatYear = SimpleDateFormat("yyyy", Locale.US)
        val dateParsed = dateConverter.parse(date)

        //Get litersUsed
        val minutes = binding.activityMinutesEdt.text.toString().toInt()
        val litersUsed: Float = when(binding.activityTypeSpinner.text.toString()){
            "Bañarse" -> minutes * useData.showerLts //From OMS
            "Usar el retrete" -> minutes * useData.wcLts
            "Lavarse las manos" -> minutes * useData.handsLts
            "Cepillarse los dientes" -> minutes * useData.brushLts
            "Afeitarse" -> minutes * useData.shaveLts
            "Lavar los trastes" -> minutes * useData.dishesLts
            "Lavar el coche" -> minutes * useData.carLts
            "Regar plantas" -> minutes * useData.plantsLts
            else -> 0.0F
        }

        //Check if rainWater
        val radioButtonId = binding.waterUsedRadioGroup.checkedRadioButtonId
        val radioButton = binding.waterUsedRadioGroup.findViewById<RadioButton>(radioButtonId)
        val isRainWater = isRainWater(radioButton)

        val newLog = WaterUseLog(
            0,
            userPreferences.getString("uid","")!!,
            dateParsed,
            formatYear.format(dateParsed),
            formatMonth.format(dateParsed),
            formatDay.format(dateParsed),
            binding.activityTypeSpinner.text.toString(),
            minutes,
            litersUsed,
            isRainWater
        )

        database.collection("water_use_log")
            .add(newLog)
            .addOnSuccessListener {
                Log.d("Saved", "Success")
                binding.saveActivityProgressBar.isVisible = false
                CoroutineScope(Dispatchers.IO).launch {
                    registerActivityViewModel.insertLog(newLog)
                }
                findNavController().popBackStack()
            }

    }

    private fun isRainWater(radioButton: RadioButton): Boolean {
        if(radioButton.text.toString() == "Agua de lluvia"){
            return true
        }
        return false
    }

    private fun showDatePickerDialog(view: View){
        val newFragment = DatePickerFragment(view)
        newFragment.show(requireActivity().supportFragmentManager, "datePicker")
    }

    class DatePickerFragment(it: View) : DialogFragment(), DatePickerDialog.OnDateSetListener {

        private val v = it

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            return DatePickerDialog(requireActivity(), this, year, month, day)
        }

        override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
            val edt = v as EditText
            val selectedDate = "$p1-${p2+1}-$p3"
            edt.setText(selectedDate)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterActivityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterActivityFragment().apply {
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
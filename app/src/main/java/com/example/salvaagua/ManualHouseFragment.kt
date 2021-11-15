package com.example.salvaagua

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.salvaagua.databinding.FragmentManualHouseBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ManualHouseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManualHouseFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentManualHouseBinding? = null
    private val binding get() = _binding!!

    lateinit var housePreferences: SharedPreferences
    lateinit var userPreferences: SharedPreferences
    lateinit var startupPreferences: SharedPreferences
    lateinit var database : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        startupPreferences = requireActivity().getSharedPreferences("startup", AppCompatActivity.MODE_PRIVATE)
        housePreferences = requireActivity().getSharedPreferences("house", AppCompatActivity.MODE_PRIVATE)
        userPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        database = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentManualHouseBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        (activity as AppCompatActivity).supportActionBar?.setCustomView(R.layout.custom_action_bar)
        val d = (activity as AppCompatActivity).supportActionBar?.customView as LinearLayout
        val text = (d.getChildAt(0) as TextView)
        text.text = "Datos del Hogar"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerArray= arrayListOf<String>()

        database.collection("precipitation").orderBy("location").get()
            .addOnSuccessListener {
                for(i in 0 until it.documents.size){
                    spinnerArray.add(it.documents[i].getString("location")!!)

                }
                val adapter: ArrayAdapter<String> = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, spinnerArray)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.locationSpn.setAdapter(adapter)
            }

        binding.saveManualHouseBtn.setOnClickListener {
            val editor = housePreferences.edit()
            editor.putString("house_name", binding.houseNameEdt.text.toString())
            editor.putString("location", binding.locationSpn.text.toString())
            editor.putFloat("roof_area", binding.roofAreaEdt.text.toString().toFloat())
            editor.putFloat("month_use", binding.monthUseEdt.text.toString().toFloat())
            editor.putInt("house_members", binding.houseMembersEdt.text.toString().toInt())
            editor.apply()

            val houseData : MutableMap<String, Any> = HashMap()
            houseData["user_id"] = userPreferences.getString("uid", "").toString()
            houseData["has_sensors"] = housePreferences.getBoolean("has_sensors", false)
            houseData["house_name"] = housePreferences.getString("house_name", "").toString()
            houseData["location"] = housePreferences.getString("location","").toString()
            houseData["roof_area"] = housePreferences.getFloat("roof_area", 0.0F)
            houseData["house_members"] = housePreferences.getInt("house_members", 0)
            houseData["month_use"] = housePreferences.getFloat("month_use", 0.0F)
            database.collection("houses")
                .add(houseData)
                .addOnSuccessListener {
                    startupPreferences.edit().putBoolean("house_setup", true).apply()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error guardando datos, intente de nuevo",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ManualHouseFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ManualHouseFragment().apply {
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
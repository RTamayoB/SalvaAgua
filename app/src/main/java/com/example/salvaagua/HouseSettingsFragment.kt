package com.example.salvaagua

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.salvaagua.databinding.FragmentHouseSettingsBinding
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HouseSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HouseSettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentHouseSettingsBinding? = null
    private val binding get() = _binding!!

    lateinit var housePreferences: SharedPreferences
    lateinit var database : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        housePreferences = requireActivity().getSharedPreferences("house", AppCompatActivity.MODE_PRIVATE)
        database = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHouseSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roomsList = binding.roomsList
        val houseNameEdt = binding.houseNameEdt
        val roofAreaEdt = binding.roofAreaEdt

        binding.saveHouseBtn.setOnClickListener {
            //TODO: Add roof_material (runoff) and precipitation level (location)
            val house: HashMap<String, Any> = HashMap()
            house["house_name"] = houseNameEdt.text.toString()
            house["roof_area"] = roofAreaEdt.text.toString().toFloat()
            database.collection("houses")
                .add(house)
                .addOnSuccessListener {
                    val editor = housePreferences.edit()
                    editor.putString("house_id", it.id)
                    editor.putString("house_name", houseNameEdt.text.toString())
                    editor.putString("roof_area", roofAreaEdt.text.toString())
                    editor.apply()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                .addOnFailureListener {
                    Log.d("HouseSettingFragment", "Error: $it")
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
         * @return A new instance of fragment HouseSettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HouseSettingsFragment().apply {
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
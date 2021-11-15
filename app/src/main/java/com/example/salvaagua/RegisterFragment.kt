package com.example.salvaagua

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.salvaagua.databinding.FragmentRegisterBinding
import com.example.salvaagua.viewmodels.RegisterViewModel
import com.example.salvaagua.viewmodels.RegisterViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var registerViewModel: RegisterViewModel
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    lateinit var startupPreferences: SharedPreferences
    lateinit var userPreferences: SharedPreferences
    lateinit var database : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        auth = Firebase.auth
        startupPreferences = requireActivity().getSharedPreferences("startup", AppCompatActivity.MODE_PRIVATE)
        userPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        database = FirebaseFirestore.getInstance()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        (activity as AppCompatActivity).supportActionBar?.setCustomView(R.layout.custom_action_bar)
        val d = (activity as AppCompatActivity).supportActionBar?.customView as LinearLayout
        val text = (d.getChildAt(0) as TextView)
        text.text = "Registrarse"

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory())
            .get(RegisterViewModel::class.java)

        val nameEdt = binding.setNameEdt
        val lastNameEdt = binding.setLastnameEdt
        val emailEdt = binding.setEmailEdt
        val passwordEdt = binding.setPasswordEdt
        val confirmPasswordEdt = binding.confirmPasswordEdt
        val registerBtn = binding.registerBtn

        registerBtn.isEnabled = false
        registerViewModel.registerFormState.observe(viewLifecycleOwner,
            Observer { registerFormState ->
                if(registerFormState == null){
                    return@Observer
                }
                registerBtn.isEnabled = registerFormState.isDataValid
                registerFormState.nameError?.let {
                    nameEdt.error = getString(it)
                }
                registerFormState.lastNameError?.let {
                    lastNameEdt.error = getString(it)
                }
                registerFormState.emailError?.let {
                    emailEdt.error = getString(it)
                }
                registerFormState.passwordError?.let {
                    passwordEdt.error = getString(it)
                }
                registerFormState.confirmPasswordError?.let {
                    confirmPasswordEdt.error = getString(it)
                }
            }
        )

        val afterTextChangedListener = object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //
            }

            override fun afterTextChanged(p0: Editable?) {
                registerViewModel.registerDataChanged(
                    nameEdt.text.toString(),
                    lastNameEdt.text.toString(),
                    emailEdt.text.toString(),
                    passwordEdt.text.toString(),
                    confirmPasswordEdt.text.toString()
                )
            }

        }
        nameEdt.addTextChangedListener(afterTextChangedListener)
        lastNameEdt.addTextChangedListener(afterTextChangedListener)
        emailEdt.addTextChangedListener(afterTextChangedListener)
        passwordEdt.addTextChangedListener(afterTextChangedListener)
        confirmPasswordEdt.addTextChangedListener(afterTextChangedListener)
        confirmPasswordEdt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerViewModel.register(
                    nameEdt.text.toString(),
                    lastNameEdt.text.toString(),
                    emailEdt.text.toString(),
                    passwordEdt.text.toString(),
                    confirmPasswordEdt.text.toString()
                )
            }
            false
        }

        binding.registerBtn.setOnClickListener {
            binding.registerPrgBar.isVisible = true
            //Save user
            auth.createUserWithEmailAndPassword(emailEdt.text.toString(), passwordEdt.text.toString())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        binding.registerPrgBar.isVisible = false
                        val user = auth.currentUser
                        val newUser: MutableMap<String, Any> = HashMap()
                        newUser["email"] = emailEdt.text.toString()
                        newUser["name"] = nameEdt.text.toString()
                        newUser["last_name"] = lastNameEdt.text.toString()
                        database.collection("users").document(user!!.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                startupPreferences.edit().putBoolean("register", true).apply()
                                val editor = userPreferences.edit()
                                editor.putString("uid", user.uid)
                                editor.putString("email", emailEdt.text.toString())
                                editor.putString("name", nameEdt.text.toString())
                                editor.putString("last_name", lastNameEdt.text.toString())
                                editor.apply()
                                findNavController().navigate(R.id.action_registerFragment_to_modeFragment)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al registrarse",
                                    Toast.LENGTH_SHORT).show()
                            }
                    }
                    else{
                        binding.registerPrgBar.isVisible = false
                        Toast.makeText(context, "Error al registrarse",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.loginTxt.setOnClickListener {
            startupPreferences.edit().putBoolean("register", true).apply()
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
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
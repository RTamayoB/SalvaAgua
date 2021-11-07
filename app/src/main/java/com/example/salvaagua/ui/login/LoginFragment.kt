package com.example.salvaagua.ui.login

import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.salvaagua.MainActivity
import com.example.salvaagua.databinding.FragmentLoginBinding

import com.example.salvaagua.R
import com.example.salvaagua.SetupActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var auth : FirebaseAuth

    lateinit var housePreferences: SharedPreferences
    lateinit var startupPreferences: SharedPreferences
    lateinit var userPreferences: SharedPreferences
    lateinit var database : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        housePreferences = requireActivity().getSharedPreferences("house", AppCompatActivity.MODE_PRIVATE)
        startupPreferences = requireActivity().getSharedPreferences("startup", AppCompatActivity.MODE_PRIVATE)
        userPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        database = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        val loadingProgressBar = binding.loading

        if(auth.currentUser != null){
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }

        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loginResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            loginViewModel.login(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
            auth.signInWithEmailAndPassword(usernameEditText.text.toString(), passwordEditText.text.toString())
                .addOnSuccessListener {
                    val uid: String = it.user!!.uid
                    startupPreferences.edit().putBoolean("register", true).apply()
                    val editor = userPreferences.edit()
                    database.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { user ->
                            editor.putString("email", user.getString("email"))
                            editor.putString("name", user.getString("name"))
                            editor.putString("last_name", user.getString("last_name"))
                            editor.apply()

                            database.collection("houses")
                                .whereEqualTo("user_id",uid)
                                .get()
                                .addOnSuccessListener { houses ->
                                    val house = houses.documents[0]
                                    val houseEditor = housePreferences.edit()
                                    houseEditor.putString("house_name", house.getString("house_name"))
                                    houseEditor.putString("location", house.getString("location"))
                                    houseEditor.putFloat("roof_area", house.getDouble("roof_area")!!.toFloat())
                                    houseEditor.putFloat("month_use", house.getDouble("month_use")!!.toFloat())
                                    houseEditor.putInt("house_members", house.getDouble("house_members")!!.toInt())
                                    houseEditor.apply()
                                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                                }
                        }


                }
        }

        binding.registerTxt.setOnClickListener {
            startupPreferences.edit().putBoolean("register", false).apply()
            startActivity(Intent(requireActivity(), SetupActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.salvaagua

import android.Manifest.permission
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.salvaagua.databinding.FragmentMainBinding
import com.example.salvaagua.viewmodels.MainFragmentViewModel
import com.example.salvaagua.viewmodels.MainFragmentViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import com.example.salvaagua.data.entities.WaterUseLog
import com.example.salvaagua.util.WaterUseData
import com.example.salvaagua.viewmodels.ChartsViewModel
import com.example.salvaagua.viewmodels.ChartsViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList
import android.content.pm.PackageManager
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.io.*
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    lateinit var startupPreferences: SharedPreferences
    lateinit var userPreferences: SharedPreferences
    lateinit var housePreferences: SharedPreferences
    lateinit var database: FirebaseFirestore

    lateinit var registerUseFAB: FloatingActionButton
    lateinit var chartsTabLayout: TabLayout

    private lateinit var chartPagerAdapter: ChartPagerAdapter


    var pageHeight = 1120
    var pagewidth = 792
    var bmp: Bitmap? = null
    var scaledbmp:Bitmap? = null
    private lateinit var currentData : List<WaterUseLog>
    private var pdfTitle = ""

    private val mainFragmentViewModel: MainFragmentViewModel by viewModels {
        MainFragmentViewModelFactory((requireActivity().application as MyApplication).waterUseLogRepository)
    }
    private val chartsViewModel: ChartsViewModel by viewModels {
        ChartsViewModelFactory((requireActivity().application as MyApplication).waterUseLogRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        startupPreferences = requireActivity().getSharedPreferences("startup", AppCompatActivity.MODE_PRIVATE)
        userPreferences = requireActivity().getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE)
        housePreferences = requireActivity().getSharedPreferences("house", AppCompatActivity.MODE_PRIVATE)
        database = FirebaseFirestore.getInstance()
        Log.d("BMP", bmp.toString())

        if (checkPermission()) {
            Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        (activity as AppCompatActivity).supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        (activity as AppCompatActivity).supportActionBar?.setCustomView(R.layout.custom_action_bar)
        val d = (activity as AppCompatActivity).supportActionBar?.customView as LinearLayout
        val text = (d.getChildAt(0) as TextView)
        text.text = "Salva Agua"
    }

    private fun checkPermission(): Boolean {
        // checking of permissions.
        val permission1 = ContextCompat.checkSelfPermission(requireContext(),
            WRITE_EXTERNAL_STORAGE
        )
        val permission2 = ContextCompat.checkSelfPermission(
            requireContext(),
            READ_EXTERNAL_STORAGE
        )
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            200
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 200) {
            if (grantResults.isNotEmpty()) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                val writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (writeStorage && readStorage) {
                    Toast.makeText(requireContext(), "Permission Granted..", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Permission Denined.", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        registerUseFAB = binding.registerUseFab
        chartsTabLayout = binding.chartsTabLayout

        val llm = LinearLayoutManager(requireActivity())
        llm.orientation = LinearLayoutManager.VERTICAL
        binding.activityRecyclerView.layoutManager = llm

        chartPagerAdapter = ChartPagerAdapter(this)
        binding.chartsViewPager.adapter = chartPagerAdapter
        TabLayoutMediator(binding.chartsTabLayout, binding.chartsViewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Dia"
                1 -> tab.text = "Semana"
                2 -> tab.text = "Mes"
                else -> tab.text = "Año"
            }
        }.attach()

        binding.activityRecyclerView.addItemDecoration(DividerItemDecoration(binding.activityRecyclerView.context, DividerItemDecoration.VERTICAL))

        if(!startupPreferences.getBoolean("set_goal", false)){
            val goalEdt = EditText(requireActivity())
            goalEdt.hint = "Litros diarios"
            goalEdt.width = 50
            goalEdt.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            AlertDialog.Builder(requireContext())
                .setTitle("Establece una meta de consumo diaria")
                .setMessage("¡Es momento de ahorrar! Elija su meta de consumo diario, " +
                        "puede cambiarla en cualquier momento en configuraciones (La OMS recomienda" +
                        "un uso al dia de 100 litros por persona).")
                .setView(goalEdt)
                .setPositiveButton("Guardar") { _, _ ->
                    housePreferences.edit().putFloat("goal_percentage", goalEdt.text.toString().toFloat()).apply()
                    startupPreferences.edit().putBoolean("set_goal", true).apply()
                }
                .setCancelable(false)
                .show()
        }

        binding.registerUseFab.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_registerActivityFragment)
        }

        binding.chartsTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //Depending on tab selection show the percentage of catchment water to be used
                Log.d("Tab Selected", tab?.text.toString())
                when(tab?.text){
                    "Dia" -> showDayData()
                    "Semana" -> showWeeklyData()
                    "Mes" -> showMonthData()
                    else -> showYearData()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        when(chartsTabLayout.selectedTabPosition){
            0 -> showDayData()
            1 -> showWeeklyData()
            2 -> showMonthData()
            else -> showYearData()
        }


        binding.generateReportBtn.setOnClickListener {
            //TODO: Add pdf title
            createPdf(pdfTitle,currentData)
        }

        return binding.root
    }

    fun showDayData() {
        binding.percentageCardview.visibility = View.GONE
        val today = Calendar.getInstance().time
        chartsViewModel.waterUseLogByDate(today)
            .observe(requireActivity()) { logs ->
                currentData = logs
                val titleParser = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(today)
                pdfTitle = titleParser
                if(logs.isEmpty()){
                    binding.activitiesLbl.visibility = View.GONE
                }
                //Check each log, group in a logMap, with key activity, child a list with (numbers of use, minutes, water used)
                val activityMap = linkedMapOf<String, ArrayList<Any>>()
                for(i in logs.indices){
                    val log = logs[i]
                    if(activityMap[log.activity] == null){
                        val record = arrayListOf<Any>()
                        record.add(1)
                        record.add(log.minutes)
                        record.add(log.waterUsed)
                        activityMap[log.activity] = record
                    }
                    else{
                        val recordToUpdate : ArrayList<Any>? = activityMap[log.activity]
                        val use = recordToUpdate?.get(0) as Int
                        val minutes = recordToUpdate[1] as Int
                        val waterUsed = recordToUpdate[2] as Float
                        recordToUpdate[0] = use + 1
                        recordToUpdate[1] = minutes + log.minutes
                        recordToUpdate[2] = waterUsed + log.waterUsed
                        activityMap[log.activity] = recordToUpdate
                    }
                }
                val items = arrayListOf<ActivityItem>()
                for((activity, data) in activityMap){
                    val info = isOverUse(activity, data[0] as Int, data[1] as Int, data[2] as Float) as ArrayList<String>
                    items.add(ActivityItem(activity,data[2] as Float, info[0], info[1]))
                }
                binding.activityRecyclerView.adapter = ActivityAdapter(items, requireContext())
            }
    }

    fun showWeeklyData(){
        binding.percentageCardview.visibility = View.GONE
        val date = Calendar.getInstance()
        val date1 = Calendar.getInstance()
        date.add(Calendar.DATE, -3)
        val startDate = date.time
        date1.add(Calendar.DATE,3)
        val endDate = date1.time
        chartsViewModel.waterUseLogByWeek(
            startDate,
            endDate
        ).observe(requireActivity()) { logs ->
            currentData = logs
            val startParsed = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(startDate)
            val endParsed = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(endDate)
            pdfTitle = "$startParsed - $endParsed"
            if(logs.isEmpty()){
                binding.activitiesLbl.visibility = View.GONE
            }
            val activityMap = linkedMapOf<String, ArrayList<Any>>()
            for(i in logs.indices){
                val log = logs[i]
                if(activityMap[log.activity] == null){
                    val record = arrayListOf<Any>()
                    record.add(1)
                    record.add(log.minutes)
                    record.add(log.waterUsed)
                    activityMap[log.activity] = record
                }
                else{
                    val recordToUpdate : ArrayList<Any>? = activityMap[log.activity]
                    val use = recordToUpdate?.get(0) as Int
                    val minutes = recordToUpdate[1] as Int
                    val waterUsed = recordToUpdate[2] as Float
                    recordToUpdate[0] = use + 1
                    recordToUpdate[1] = minutes + log.minutes
                    recordToUpdate[2] = waterUsed + log.waterUsed
                    activityMap[log.activity] = recordToUpdate
                }
            }

            val items = arrayListOf<ActivityItem>()
            for((activity, data) in activityMap){
                val info = isOverUse(activity, data[0] as Int, data[1] as Int, data[2] as Float) as ArrayList<String>
                items.add(ActivityItem(activity,data[2] as Float, info[0], info[1]))
            }

            binding.activityRecyclerView.adapter = ActivityAdapter(items, requireContext())
        }
    }

    fun showMonthData(){
        binding.percentageCardview.visibility = View.VISIBLE
        var precipitation: Double
        val date = Calendar.getInstance()
        database.collection("precipitation")
            .whereEqualTo("location", housePreferences.getString("location", ""))
            .get()
            .addOnSuccessListener {
                precipitation = it.documents[0].getDouble(date.get(Calendar.MONTH).toString())!!
                chartsViewModel.waterUseLogByMonth(
                    (date.get(Calendar.MONTH)+1).toString(),
                    date.get(Calendar.YEAR).toString()
                ).observe(requireActivity()) { logs ->
                    currentData = logs
                    pdfTitle = "Mes ${date.get(Calendar.MONTH)}"
                    var total = 0.0
                    if(logs.isEmpty()){
                        binding.activitiesLbl.visibility = View.GONE
                    }
                    val activityMap = linkedMapOf<String, ArrayList<Any>>()
                    for(i in logs.indices){
                        val log = logs[i]
                        if(activityMap[log.activity] == null){
                            val record = arrayListOf<Any>()
                            record.add(1)
                            record.add(log.minutes)
                            record.add(log.waterUsed)
                            activityMap[log.activity] = record
                        }
                        else{
                            val recordToUpdate : ArrayList<Any>? = activityMap[log.activity]
                            val use = recordToUpdate?.get(0) as Int
                            val minutes = recordToUpdate[1] as Int
                            val waterUsed = recordToUpdate[2] as Float
                            recordToUpdate[0] = use + 1
                            recordToUpdate[1] = minutes + log.minutes
                            recordToUpdate[2] = waterUsed + log.waterUsed
                            activityMap[log.activity] = recordToUpdate
                        }
                    }
                    val items = arrayListOf<ActivityItem>()
                    for((activity, data) in activityMap){
                        total += data[2] as Float
                        val info = isOverUse(activity, data[0] as Int, data[1] as Int, data[2] as Float) as ArrayList<String>
                        items.add(ActivityItem(activity,data[2] as Float, info[0], info[1]))
                    }

                    binding.activityRecyclerView.adapter = ActivityAdapter(items, requireContext())


                    val percentage : Double = (precipitation / total) * 100
                    binding.catchmentLbl.text = "Este mes, el agua de lluvia cubre"
                    showPercentage(percentage)
                }
            }
    }

    fun showYearData() {
        binding.percentageCardview.visibility = View.VISIBLE
        var precipitation: Double = 0.0
        val date = Calendar.getInstance()
        database.collection("precipitation")
            .whereEqualTo("location", housePreferences.getString("location", ""))
            .get()
            .addOnSuccessListener {
                for(i in 0 until 12){
                    precipitation += it.documents[0].getDouble(i.toString())!!
                }
                chartsViewModel.waterUseLogByYear(
                    date.get(Calendar.YEAR).toString())
                    .observe(requireActivity()) { logs ->
                        currentData = logs
                        pdfTitle = "Año ${date.get(Calendar.YEAR)}"
                        var total = 0.0
                        if(logs.isEmpty()){
                            binding.activitiesLbl.visibility = View.GONE
                        }
                        val activityMap = linkedMapOf<String, ArrayList<Any>>()
                        for(i in logs.indices){
                            val log = logs[i]
                            if(activityMap[log.activity] == null){
                                val record = arrayListOf<Any>()
                                record.add(1)
                                record.add(log.minutes)
                                record.add(log.waterUsed)
                                activityMap[log.activity] = record
                            }
                            else{
                                val recordToUpdate : ArrayList<Any>? = activityMap[log.activity]
                                val use = recordToUpdate?.get(0) as Int
                                val minutes = recordToUpdate[1] as Int
                                val waterUsed = recordToUpdate[2] as Float
                                recordToUpdate[0] = use + 1
                                recordToUpdate[1] = minutes + log.minutes
                                recordToUpdate[2] = waterUsed + log.waterUsed
                                activityMap[log.activity] = recordToUpdate
                            }
                        }
                        val items = arrayListOf<ActivityItem>()
                        for((activity, data) in activityMap){
                            total += data[2] as Float
                            val info = isOverUse(activity, data[0] as Int, data[1] as Int, data[2] as Float) as ArrayList<String>
                            items.add(ActivityItem(activity,data[2] as Float, info[0], info[1]))
                        }
                        binding.activityRecyclerView.adapter = ActivityAdapter(items, requireContext())

                        val percentage : Double = (precipitation / total) * 100
                        binding.catchmentLbl.text = "Este año, el agua de lluvia cubre"
                        showPercentage(percentage)
                    }
            }
    }

    private fun showPercentage(percentage: Double){
        if(percentage <= 30){
            binding.catchmentTxt.setTextColor(Color.RED)
        }
        else if(percentage > 30 && percentage < 80){
            binding.catchmentTxt.setTextColor(Color.YELLOW)
        }
        else{
            binding.catchmentTxt.setTextColor(Color.GREEN)
        }
        when {
            percentage >= 100 -> {
                binding.catchmentTxt.text = "100%"
            }
            percentage <= 0 -> {
                binding.catchmentTxt.text = "0%"
            }
            else -> {
                binding.catchmentTxt.text = "${String.format("%.2f", percentage)}%"
            }
        }
    }

    private fun isOverUse(activity: String, timesUsed: Int, minutes: Int, waterUsed: Float): ArrayList<String>? {
        val useData = WaterUseData()
        val resultArray = arrayListOf("","")
        when(activity) {
            "Bañarse" -> {
                val targetWater = useData.showerLts * minutes
                val averageMinutes = minutes/timesUsed
                if(averageMinutes > useData.showerMinutes){
                    resultArray[1] = resultArray[1] + "Consejo: La OMS recomienda que el baño promedio dure 5 minutos."
                }
                if(waterUsed > targetWater){
                    resultArray[0] = "Sobre Consumo"
                }
                else{
                    resultArray[0] = "Buen Consumo"
                }
                return resultArray
            }
            "Usar el retrete" -> {
                val targetWater = useData.wcLts * minutes
                val averageMinutes = minutes/timesUsed
                if(waterUsed > targetWater){
                    resultArray[0] = "Sobre Consumo"
                    resultArray[1] = resultArray[1] + "Consejo: Recuerda no pasar el tiempo en el WC viendo tu telefono celular."
                }
                else{
                    resultArray[0] = "Buen Consumo"
                }
                return resultArray
            }
            "Lavarse las manos" -> {
                val targetWater = useData.handsLts * minutes
                val averageMinutes = minutes/timesUsed
                if(averageMinutes > useData.handsMinutes){
                    resultArray[1] = resultArray[1] + "Consejo: La OMS recomienda que el lavado de mano dure menos de un minuto."
                }
                if(waterUsed > targetWater){
                    resultArray[0] = "Sobre Consumo"
                }
                else{
                    resultArray[0] = "Buen Consumo"
                }
                return resultArray
            }
            "Cepillarse los dientes" -> {
                val targetWater = useData.brushLts * minutes
                val averageMinutes = minutes/timesUsed
                if(averageMinutes > useData.brushMinutes){
                    resultArray[1] = resultArray[1] + "Consejo: Recuerda que el cepillado de dientes debe durar entre 2 a 3 minutos."
                }
                if(waterUsed > targetWater){
                    resultArray[1] = resultArray[1] + " Cuando te cepilles los dientes, procura solo tener el grifo abierto al mojar y limpiar tu cepillo."
                    resultArray[0] = "Sobre Consumo"
                }
                else{
                    resultArray[0] = "Buen Consumo"
                }
                return resultArray
            }
            "Afeitarse" -> {
                val targetWater = useData.shaveLts * minutes
                val averageMinutes = minutes/timesUsed
                if(averageMinutes > useData.shaveMinutes){
                    //
                }
                if(waterUsed > targetWater){
                    resultArray[1] = resultArray[1] + "Consejo: Al afeitarte solo abre el grifo cuando sea necesario."
                    resultArray[0] = "Sobre Consumo"
                }
                else{
                    resultArray[0] = "Buen Consumo"
                }
                return resultArray
            }
            "Lavar los trastes" -> {
                val targetWater = useData.dishesLts * minutes
                val averageMinutes = minutes/timesUsed
                if(averageMinutes > useData.dishesMinutes){
                    resultArray[1] = resultArray[1] + "Consejo: Intenta mantener el lavado de trastes por debajo de 30 minutos."
                }
                if(waterUsed > targetWater){
                    resultArray[1] = resultArray[1] + " Solo abre el grifo cuando tengas que enjuagar los trastes."
                    resultArray[0] = "Sobre Consumo"
                }
                else{
                    resultArray[0] = "Buen Consumo"
                }
                return resultArray
            }
            "Lavar el coche" -> {
                val targetWater = useData.carLts * minutes
                val averageMinutes = minutes/timesUsed
                if(averageMinutes > useData.carMinutes){
                    resultArray[1] = resultArray[1] + "Consejo: Manten el lavado de coche por debajo de 40 minutos."
                }
                if(waterUsed > targetWater){
                    resultArray[0] = "Sobre Consumo"
                }
                else{
                    resultArray[0] = "Buen Consumo"
                }
                return resultArray
            }
            "Regar plantas" -> {
                val targetWater = useData.plantsLts * minutes
                val averageMinutes = minutes/timesUsed
                if(averageMinutes > useData.plantsMinutes){
                }
                if(waterUsed > targetWater){
                    resultArray[1] = resultArray[1] + "Consejo: Riega las plantas con una cubeta en vez de utilizar una manguera para ahorrar agua."
                    resultArray[0] = "Sobre Consumo"
                }
                else{
                    resultArray[0] = "Buen Consumo"
                }
                return resultArray
            }
        }
        return arrayListOf("","")
    }

    private fun createPdf(pdfTitle: String, logs: List<WaterUseLog>) {

        try {
            val pdfDocument = PdfDocument(PdfWriter(requireContext().getExternalFilesDir(null)!!.absolutePath+"/$pdfTitle"))
            val document = Document(pdfDocument)

            val res = requireActivity().resources
            val drawable = ResourcesCompat.getDrawable(res,R.drawable.app_logo_text,requireContext().theme)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val finalBitmap = Bitmap.createScaledBitmap(mutableBitmap,160,80,false)
            Log.d("Image Sizes", "Width:${mutableBitmap.width} - Height:${mutableBitmap.height}")

            val stream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val bitMapData = stream.toByteArray()
            val imageData = ImageDataFactory.create(bitMapData)
            val img = Image(imageData)
            img.setTextAlignment(TextAlignment.RIGHT)
            document.add(img)
            //val name = Paragraph("Salva Agua").setTextAlignment(TextAlignment.CENTER)
            //document.add(name)
            val title = Paragraph(pdfTitle).setTextAlignment(TextAlignment.LEFT)
            document.add(title)

            val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 30f, 10f, 10f))).useAllAvailableWidth()
            table.addHeaderCell(Cell().add(Paragraph("Fecha").setTextAlignment(TextAlignment.CENTER).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("Actividad").setTextAlignment(TextAlignment.CENTER).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("Minutos").setTextAlignment(TextAlignment.CENTER).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("Agua").setTextAlignment(TextAlignment.CENTER).setBold()))

            for (log in logs){
                val dateParsed = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(log.date!!)

                table.addCell(Cell().add(Paragraph(dateParsed).setTextAlignment(TextAlignment.CENTER)))
                table.addCell(Cell().add(Paragraph(log.activity).setTextAlignment(TextAlignment.CENTER)))
                table.addCell(Cell().add(Paragraph(log.minutes.toString()).setTextAlignment(TextAlignment.CENTER)))
                table.addCell(Cell().add(Paragraph(log.waterUsed.toString()).setTextAlignment(TextAlignment.CENTER)))
            }

            document.add(table)
            document.close()
            Toast.makeText(requireContext(),"Reporte Generado", Toast.LENGTH_SHORT).show()

            val file = File(requireContext().getExternalFilesDir(null)?.absolutePath+"/$pdfTitle")
            val target = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
            target.setDataAndType(uri, "application/pdf")
            target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val intent = Intent.createChooser(target,"Abrir Reporte")
            try {
                startActivity(intent)
            }catch (e: Exception){
                Log.d("Error Opening Pdf", e.toString())
            }
        }catch (e: Exception){
            Log.d("Error Creating Pdf", e.toString())
        }
    }

    @Throws(IOException::class)
    fun getImage(url: URL): ByteArray? {
        val baos = ByteArrayOutputStream()
        val `is`: InputStream = url.openStream()
        val b = ByteArray(4096)
        var n: Int
        while (`is`.read(b).also { n = it } > -1) {
            baos.write(b, 0, n)
        }
        return baos.toByteArray()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
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
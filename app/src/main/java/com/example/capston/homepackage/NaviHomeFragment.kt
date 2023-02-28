package com.example.capston.homepackage

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.capston.MainActivity
import com.example.capston.R
import com.example.capston.WalkFragment
import com.example.capston.databinding.FragmentNaviHomeBinding
import kotlinx.android.synthetic.main.fragment_navi_home.*
import java.text.SimpleDateFormat
import java.util.*

var RecordPage = HomeRecord()
val walkFragment = WalkFragment()

class NaviHomeFragment : Fragment(), View.OnClickListener {

    var mNow: Long = 0
    var mDate: Date? = null
    var mFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    var mTextView: TextView? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentNaviHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNaviHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        //bind view
//        mTextView = binding.day
//        getTime()

        binding.recordbtn.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.main_frm, walkFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        binding.recordbtn.setOnClickListener{
//            onClick(view)
//        }
    }

    // fragment 액션바 보여주기(선언안해주면 다른 프레그먼트에서 선언한 .hide() 때문인지 모든 프레그먼트에서 액션바 안보임
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        setupData()
        setupStatusHandler()

//        val mapView = MapView(context)
//        binding.kakaoMapView.addView(mapView)


    }

    lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity

    }

    fun goRecordPage(){
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frm, RecordPage)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NaviHomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
    //메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupData() {

        val statusData = resources.getStringArray(R.array.spinner_ddong)

        val statusAdapter = object : ArrayAdapter<String>(requireContext(),R.layout.gender_spinner) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val v = super.getView(position, convertView, parent)
                var tv = v as TextView
                tv.setTextSize(/* size = */ 16F)
                if (position == count) {

                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).text = ""
                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).hint = " 선택"

                }
                return v
            }

            override fun getCount(): Int {
                return super.getCount() - 1
            }

        }

        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        statusAdapter.addAll(statusData.toMutableList())
        statusAdapter.add(" 선택")

        _binding!!.dogDdongSpinner.adapter = statusAdapter

        dog_ddong_spinner.setSelection(statusAdapter.count)
        dog_ddong_spinner.dropDownVerticalOffset = dipToPixels(15f).toInt()
    }


    private fun setupStatusHandler() {
        _binding?.dogDdongSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when(position) {
                    0 -> {

                    }
                    else -> {

                    }
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }


    private fun dipToPixels(dipValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dipValue,
            resources.displayMetrics
        )
    }

//    private fun getTime(): String? {
//        mNow = System.currentTimeMillis()
//        mDate = Date(mNow)
//        mTextView?.text = getTime();
//        return mFormat.format(mDate)
//    }


    override fun onClick(view: View?) {
        val dlg = MyDialog(mainActivity)

        dlg.setOnOKClickedListener{ content ->
            binding.recordbtn.text = content
        }
        dlg.show()
    }
}
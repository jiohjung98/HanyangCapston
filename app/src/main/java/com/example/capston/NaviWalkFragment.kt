package com.example.capston

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.FragmentNaviWalkBinding
import com.example.capston.homepackage.WalkDialog
import kotlinx.android.synthetic.main.fragment_navi_walk.*

class NaviWalkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentNaviWalkBinding?= null
    private val binding get() = _binding!!

    // 1. Context를 할당할 변수를 프로퍼티로 선언(어디서든 사용할 수 있게)
    lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNaviWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.walkBtn.setOnClickListener {
            onClickWalk(view)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NaviWalkFragment().apply {
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // fragment 액션바 보여주기(선언안해주면 다른 프레그먼트에서 선언한 .hide() 때문인지 모든 프레그먼트에서 액션바 안보임
        (activity as AppCompatActivity).supportActionBar?.show()
        setupData()
        setupStatusHandler()
    }

    //메모리 누수 방지
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickWalk(view: View?) {
        val walkDlg = WalkDialog(mainActivity)
        walkDlg.setOnOKClickedListener { content ->
            binding.walkBtn.text = content
        }
        walkDlg.show("산책")
    }

    private fun setupData() {

        val statusData = resources.getStringArray(R.array.spinner_ddong)

        val statusAdapter = object : ArrayAdapter<String>(requireContext(),R.layout.gender_spinner) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val v = super.getView(position, convertView, parent)
                var tv = v as TextView
                tv.setTextSize(/* size = */ 12f)
                if (position == count) {

                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).text = ""
//                    (v.findViewById<View>(R.id.tvGenderSpinner) as TextView).hint = " 선택"

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
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
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
}
package com.example.capston

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.capston.databinding.FragmentNaviWalkBinding
import com.example.capston.homepackage.WalkDialog

/*
 *  두번째 메뉴, 산책하기 -> 다이얼로그 -> 산책지도
 */
class NaviWalkFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentNaviWalkBinding

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
        binding = FragmentNaviWalkBinding.inflate(inflater, container, false)
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
    }

    private fun onClickWalk(view: View?) {
        val walkDlg = WalkDialog(mainActivity)
        walkDlg.setOnOKClickedListener { content ->
            binding.walkBtn.text = content
        }
        walkDlg.show("산책")
    }
}
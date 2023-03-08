package com.example.capston

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_statics.*
import org.eazegraph.lib.charts.BarChart
import org.eazegraph.lib.models.BarModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Statics : AppCompatActivity() {
    var staticMonth: String = ""

    var thisMonthCalories: Double = 0.0
    var thisMonthTime: Int = 0

    var day_arr = listOf("31","28","31","30","31","30","31","31","30","31","30","31")
    var chartList =arrayListOf<BarModel>()
    var history_list = arrayListOf<StaticsItem>()
    var chartColor = listOf("#92CFBF", "#C7E2CF", "#E68A7B", "#D9B191")






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statics)

        val mBarChart: BarChart = findViewById<View>(R.id.barchart) as BarChart
        var sumKcal=findViewById<TextView>(R.id.sum_kcal_tv)
        var sumTime=findViewById<TextView>(R.id.sum_time_tv)

        //프래그먼트에서 상태바 배경색 변경하는 코드
        this.window.statusBarColor = (ContextCompat.getColor(this,
            R.color.google_login
        ))

        val historyAdapter= Statics_RVAdapter(this, history_list) { staticsItem ->
            //산책 히스토리 리스트 중 한개를 선택했을 때
            finish()    //Statics 액티비티 종료하고
//
//            val intent = Intent(this, WritePost::class.java)    //포스트작성 액티비티 열기
//            intent.putExtra("walkingId", staticsItem.walkingId)
//            startActivity(intent)
        }


        history_recyclerview.adapter=historyAdapter
        val lm = LinearLayoutManager(this)
        history_recyclerview.layoutManager = lm
        history_recyclerview.setHasFixedSize(true)


        // 산책 통계 기록 요청
        val pref = getSharedPreferences("pref", MODE_PRIVATE)
        val userToken = pref.getString("userToken", "")
        val walkingRetrofit = WalkingRetrofitCreators(this).WalkingRetrofitCreator()
        walkingRetrofit.getMyWalkingStatic(userToken!!).enqueue(object :
            Callback<MyWalkingStaticModel> {
            override fun onFailure(call: Call<MyWalkingStaticModel>, t: Throwable) {
                Log.d("DEBUG", " Static Retrofit failed!!")
                Toast.makeText(this@Statics, "산책 통계 로딩 실패. 네트워크를 확인해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
            override fun onResponse(call: Call<MyWalkingStaticModel>, response: Response<MyWalkingStaticModel>) {
                val error = response.body()?.error
                val walkings = response.body()?.walkings
                var sumStatic: MutableList<Double> = mutableListOf()
                Log.d("DEBUG", error.toString())

                walkings!!.forEach(fun(walking) {
                    val month = ReformatDate("MM", walking.date)
                    val day = ReformatDate("dd", walking.date)
                    if (staticMonth == "") {
                        staticMonth = month
                        chartList.clear()

                        staticMonth_view.text = "${month.toInt()}"

                        sumStatic = MutableList<Double>(day_arr[staticMonth.toInt()-1].toInt()) {0.0}
                    }

                    val time = walking.walkingTime
                    val hour = (time / 144000) % 24 // 1시간
                    val min = (time / 6000) % 60 // 1분
                    val sec = (time / 100) % 60 // 1초
                    var sHour: String = "--"
                    var sMin: String = "--"
                    var sSec: String = "--"

                    if (hour < 10) { // 시
                        sHour = "0$hour"
                    } else {
                        sHour = "$hour"
                    }

                    if (min < 10) { // 분
                        sMin = "0$min"
                    } else {
                        sMin = "$min"
                    }

                    if (sec < 10) {
                        sSec = "0$sec"
                    } else {
                        sSec = "$sec"
                    }

                    // 해당 Month 칼로리, 시간 합계
                    if(month == staticMonth) {
                        thisMonthCalories += walking.calories.toDouble()
                        thisMonthTime += time
                    }

                    history_list.add(StaticsItem("$month/$day", String.format("%.1f", walking.calories.toFloat()),
                        sHour, sMin, sSec, walking._id))

                    sumStatic[day.toInt()-1] = sumStatic[day.toInt()-1]+walking.calories.toFloat()

                })
                // 차트 그리기
                for (day in 0 until sumStatic.size-1) {
                    chartList.add(BarModel("${day+1}일",
                        sumStatic[day].toFloat(), Color.parseColor(chartColor[day % chartColor.size])))
                }

                historyAdapter.notifyDataSetChanged()

                // Bar 차트 Setting
                mBarChart.addBarList(chartList)
                mBarChart.barWidth = 50f
                mBarChart.barMargin = 10F
                mBarChart.animationTime = 1000
                mBarChart.startAnimation()

                sumKcal.text = String.format("%.1f", thisMonthCalories)
                sumTime.text = timeToString(thisMonthTime)

            }
        })
    }

    private fun timeToString(time: Int): String {
        val hour = (time / 144000) % 24 // 1시간
        val min = (time / 6000) % 60 // 1분
        val sec = (time / 100) % 60 // 1초
        var sHour: String = "--"
        var sMin: String = "--"
        var sSec: String = "--"

        if (hour < 10) { // 시
            sHour = "0$hour"
        } else {
            sHour = "$hour"
        }

        if (min < 10) { // 분
            sMin = "0$min"
        } else {
            sMin = "$min"
        }

        if (sec < 10) {
            sSec = "0$sec"
        } else {
            sSec = "$sec"
        }

        return "$sHour:$sMin:$sSec"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("pFlag", true)
        startActivity(intent)
        finish()
    }
}

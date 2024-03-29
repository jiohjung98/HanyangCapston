import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capston.MarkerData
import com.example.capston.R
import com.jakewharton.threetenabp.AndroidThreeTen.init
import net.daum.mf.map.api.MapPoint
import org.w3c.dom.Text

class MarkerAdapter(
    private val markerList: List<MarkerData>,
    private val itemClickListener: OnItemClickListener
    ) : RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>() {

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
//        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_community_rc_view_item, parent, false)
//        return MarkerViewHolder(itemView)
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_community_rc_view_item, parent, false)
        return MarkerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        val markerData = markerList[position]

        // 마커 정보 활용
        val date = markerData.date
        val time = markerData.time
        val breed = markerData.breed
        val imageUrl = markerData.imageUrl
        val phone = markerData.phone
        val likeAs = markerData.likeAs

        // 나머지 작업 수행
        holder.lostTimeText.text = date + " "+ time
        holder.lostBreedText.text = breed
        holder.contactNum.text = phone
        if(likeAs == 1){
            holder.like.visibility = View.VISIBLE
        }

        // 이미지 로드
        Glide.with(holder.itemView)
            .load(imageUrl)
            .placeholder(R.drawable.loading) // 로딩 중일 때 플레이스홀더 이미지
            .into(holder.lostImageView)
    }


    override fun getItemCount(): Int {
        return markerList.size
    }

    // MarkerViewHolder 클래스를 MarkerAdapter 클래스 외부로 이동시킵니다.
    inner class MarkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position)
                }
            }
        }
        val lostTimeText: TextView = itemView.findViewById(R.id.lost_time_text)
        val lostBreedText: TextView = itemView.findViewById(R.id.lost_breed_text)
        val lostImageView: ImageView = itemView.findViewById(R.id.lost_image_view)
        val contactNum : TextView = itemView.findViewById(R.id.number_text)
        val like : View = itemView.findViewById(R.id.likeas)
    }
}

interface OnItemClickListener {
    fun onItemClick(position: Int)
}
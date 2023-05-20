import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capston.MarkerData
import com.example.capston.R

class MarkerAdapter(private val markerList: List<MarkerData>) : RecyclerView.Adapter<MarkerAdapter.MarkerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_community_rc_view_item, parent, false)
        return MarkerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        val markerData = markerList[position]

        // 마커 정보 활용
        val date = markerData.time
        val breed = markerData.breed
        val imageUrl = markerData.imageUrl

        // 나머지 작업 수행
        holder.lostTimeText.text = date
        holder.lostBreedText.text = breed

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
    class MarkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lostTimeText: TextView = itemView.findViewById(R.id.lost_time_text)
        val lostBreedText: TextView = itemView.findViewById(R.id.lost_breed_text)
        val lostImageView: ImageView = itemView.findViewById(R.id.lost_image_view)
    }
}
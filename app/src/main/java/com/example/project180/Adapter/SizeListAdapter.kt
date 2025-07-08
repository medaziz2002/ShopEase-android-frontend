import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project180.R
import com.example.project180.databinding.ViewholderSizeBinding

class SizeListAdapter(private val items: List<String>, private val allowMultipleSelection: Boolean) :
    RecyclerView.Adapter<SizeListAdapter.Viewholder>() {

    private lateinit var context: Context
    private val _selectedItems = mutableListOf<String>()

    val selectedItems: List<String>
        get() = _selectedItems

    var onItemClick: ((String) -> Unit)? = null

    inner class Viewholder(val binding: ViewholderSizeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val clickedPosition = adapterPosition
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    val item = items[clickedPosition]
                    if (allowMultipleSelection) {
                        // If multiple selection is allowed
                        if (_selectedItems.contains(item)) {
                            _selectedItems.remove(item)
                        } else {
                            _selectedItems.add(item)
                        }
                    } else {
                        // If only one item can be selected, make sure to deselect others
                        _selectedItems.clear()
                        _selectedItems.add(item)
                    }
                    notifyDataSetChanged()
                    onItemClick?.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ViewholderSizeBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]
        holder.binding.sizeTxt.text = item

        if (_selectedItems.contains(item)) {
            holder.binding.sizeLayout.setBackgroundResource(R.drawable.green_bg3)
            holder.binding.sizeTxt.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            holder.binding.sizeLayout.setBackgroundResource(R.drawable.grey_bg)
            holder.binding.sizeTxt.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }

    override fun getItemCount(): Int = items.size

    fun setOnSizeSelectedListener(listener: (String) -> Unit) {
        this.onItemClick = listener
    }

    fun setSelectedItems(items: List<String>) {
        _selectedItems.clear()
        _selectedItems.addAll(items)
        notifyDataSetChanged()
    }
}

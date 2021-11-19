package jp.juggler.sampleworkermanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import jp.juggler.sampleworkermanager.GlobalState.Companion.globalState
import jp.juggler.sampleworkermanager.databinding.ActMainBinding
import jp.juggler.sampleworkermanager.databinding.LvItemBinding
import kotlinx.coroutines.launch

class ActMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalState.prepare(this)
        super.onCreate(savedInstanceState)
        setContentView(
            ActMainBinding.inflate(layoutInflater)
                .apply {
                    listView.adapter = MyAdapter()
                    btnAdd.setOnClickListener { performAdd() }
                }.root
        )
    }

    private fun performAdd() {
        lifecycleScope.launch {
            globalState.db.itemDao().upsert(RItem())
            ItemWorker.enqueue(applicationContext)
        }
    }

    inner class MyAdapter : BaseAdapter() {
        private var list: List<RItem> = emptyList()

        init {
            globalState.db.itemDao().loadLive().observe(this@ActMain) {
                list = it ?: emptyList()
                notifyDataSetChanged()
            }
        }

        override fun getCount(): Int =
            list.size

        override fun getItem(position: Int) =
            list.elementAtOrNull(position)

        override fun getItemId(position: Int) =
            list.elementAtOrNull(position)?.id ?: 0L

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            (convertView?.tag as? MyViewHolder ?: MyViewHolder(layoutInflater, parent))
                .apply { bind(list.elementAtOrNull(position)) }.viewBinding.root
    }

    class MyViewHolder(inflater: LayoutInflater, parent: ViewGroup?) {
        val viewBinding = LvItemBinding.inflate(inflater, parent, false)
            .also { it.root.tag = this }

        fun bind(item: RItem?) {
            viewBinding.tvText.text = item?.toString() ?: "null"
        }
    }
}

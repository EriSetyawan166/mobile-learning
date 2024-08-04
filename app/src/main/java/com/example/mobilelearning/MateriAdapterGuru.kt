package com.example.mobilelearning

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MateriAdapterGuru : ListAdapter<Materi, MateriGuruViewHolder>(MateriGuruDiffCallback()) {

    var onDeleteClick: ((Materi) -> Unit)? = null
    var onEditClick: ((Materi) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriGuruViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_materi_guru, parent, false)
        return MateriGuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriGuruViewHolder, position: Int) {
        val materi = getItem(position)
        holder.onDeleteClick = onDeleteClick
        holder.onEditClick = onEditClick
        holder.bind(materi)
    }
}

class MateriGuruViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var onDeleteClick: ((Materi) -> Unit)? = null
    var onEditClick: ((Materi) -> Unit)? = null
    private val judulTextView: TextView = itemView.findViewById(R.id.judulTextView)
    private val deskripsiTextView: TextView = itemView.findViewById(R.id.deskripsiTextView)
    private val imageView: ImageView = itemView.findViewById(R.id.imageView)
    private val btnView: Button = itemView.findViewById(R.id.lihatMateriButton)
    private val btnDownload: Button = itemView.findViewById(R.id.DownloadMateriButton)
    private val menuMore: ImageView = itemView.findViewById(R.id.menu_more)

    fun bind(materi: Materi) {
        judulTextView.text = materi.judul
        deskripsiTextView.text = materi.deskripsi
        imageView.setImageResource(materi.imageResId)

        btnView.setOnClickListener {
            handleViewOrDownload(materi)
        }

        btnDownload.setOnClickListener {
            downloadFile(materi)
        }

        menuMore.setOnClickListener {
            showPopupMenu(it, materi)
        }
    }

    private fun showPopupMenu(view: View, materi: Materi) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_option_materi)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    onEditClick?.invoke(materi)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick?.invoke(materi)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun downloadFile(materi: Materi) {
        val file = File(itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${materi.file_path}.pdf")

        val downloadUri = Uri.parse("${Config.BASE_URL}/${materi.file_path}")
        val request = DownloadManager.Request(downloadUri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationUri(Uri.fromFile(file))

        val downloadManager = itemView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(itemView.context, "Downloading ${materi.judul}", Toast.LENGTH_SHORT).show()
    }

    private fun handleViewOrDownload(materi: Materi) {
        val context = itemView.context
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${materi.file_path}.pdf")

        if (file.exists()) {
            openPdfActivity(context, file.absolutePath)
        } else {
            downloadAndOpenFile(context, materi)
        }
    }

    private fun openPdfActivity(context: Context, filePath: String) {
        Log.d("PDFViewerDebug", "Opening PDF at path: $filePath")
        val intent = Intent(context, PdfViewerActivity::class.java).apply {
            putExtra("PDF_FILE_PATH", filePath)
        }
        context.startActivity(intent)
    }

    private fun downloadAndOpenFile(context: Context, materi: Materi) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${materi.judul}.pdf")

        // Delete the file if it exists to ensure the latest version is downloaded
        if (file.exists()) {
            file.delete()
        }

        val downloadUri = Uri.parse("${Config.BASE_URL}/${materi.file_path}")
        Log.d("DownloadDebug", "Download URI: $downloadUri")
        val request = DownloadManager.Request(downloadUri)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationUri(Uri.fromFile(file))

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        Thread {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false
                        openPdfActivity(context, file.absolutePath)
                    }
                }
                cursor.close()
            }
        }.start()
    }


}



class MateriGuruDiffCallback : DiffUtil.ItemCallback<Materi>() {
    override fun areItemsTheSame(oldItem: Materi, newItem: Materi): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Materi, newItem: Materi): Boolean = oldItem == newItem
}
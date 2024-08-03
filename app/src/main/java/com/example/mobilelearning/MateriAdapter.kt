package com.example.mobilelearning

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MateriAdapter : ListAdapter<Materi, MateriViewHolder>(MateriDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_materi, parent, false)
        return MateriViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriViewHolder, position: Int) {
        val materi = getItem(position)
        holder.bind(materi)
    }
}

class MateriViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val judulTextView: TextView = itemView.findViewById(R.id.judulTextView)
    private val deskripsiTextView: TextView = itemView.findViewById(R.id.deskripsiTextView)
    private val imageView: ImageView = itemView.findViewById(R.id.imageView)
    private val btnView: Button = itemView.findViewById(R.id.lihatMateriButton)
    private val btnDownload: Button = itemView.findViewById(R.id.DownloadMateriButton)


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
    }

    private fun downloadFile(materi: Materi) {
        val file = File(itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${materi.judul}.pdf")

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
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${materi.judul}.pdf")

        if (file.exists()) {
            openPdfActivity(context, file.absolutePath)
        } else {
            downloadAndOpenFile(context, materi)
        }
    }

    private fun openPdfActivity(context: Context, filePath: String) {
        val intent = Intent(context, PdfViewerActivity::class.java).apply {
            putExtra("PDF_FILE_PATH", filePath)
        }
        context.startActivity(intent)
    }

    private fun downloadAndOpenFile(context: Context, materi: Materi) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "${materi.judul}.pdf")
        val downloadUri = Uri.parse("${Config.BASE_URL}/${materi.file_path}")
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


class MateriDiffCallback : DiffUtil.ItemCallback<Materi>() {
    override fun areItemsTheSame(oldItem: Materi, newItem: Materi): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Materi, newItem: Materi): Boolean = oldItem == newItem
}


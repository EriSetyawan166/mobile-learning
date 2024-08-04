package com.example.mobilelearning

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class MultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener,
    private val inputStream: InputStream,
    private val fileName: String,
    private val params: Map<String, String>
) : Request<NetworkResponse>(method, url, errorListener) {

    private val boundary = "apiclient-${System.currentTimeMillis()}"
    private val mimeType = "multipart/form-data;boundary=$boundary"

    override fun getHeaders(): Map<String, String> = mapOf("Content-Type" to mimeType)

    override fun getBody(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(byteArrayOutputStream)

        params.forEach { (key, value) ->
            dataOutputStream.writeBytes("--$boundary\r\n")
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$key\"\r\n\r\n")
            dataOutputStream.writeBytes("$value\r\n")
        }

        dataOutputStream.writeBytes("--$boundary\r\n")
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"filepdf\"; filename=\"$fileName\"\r\n")
        dataOutputStream.writeBytes("Content-Type: application/pdf\r\n\r\n")
        inputStream.copyTo(dataOutputStream)
        dataOutputStream.writeBytes("\r\n")
        dataOutputStream.writeBytes("--$boundary--\r\n")

        return byteArrayOutputStream.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<NetworkResponse> =
        Response.success(response, HttpHeaderParser.parseCacheHeaders(response))

    override fun deliverResponse(response: NetworkResponse) = listener.onResponse(response)
}
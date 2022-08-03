package com.nokopi.kotlinclient

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException

class MainActivity : AppCompatActivity() {

//    private val SERVER_IP = "192.168.11.24" //Windows notePC
//    private val SERVER_IP = "192.168.56.1" //Virtual Box
    private val SERVER_IP = "165.242.108.120" //Ubuntu DesktopPC

    private var SERVER_PORT : Int = 0

    lateinit var bitmap1: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }

//        val bitmap1: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bigimg_0007)
//        val bitmap1: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.smallimg_0020)
//        val bitmap1: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.a3kb)
        bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.img77_3)

        ImageView1.setImageBitmap(bitmap1)

        sendButton.setOnClickListener {
//            val imageEncoded = imageEncode(bitmap1)
//            connect(imageEncoded)
            val serverPort = portNumber.text.toString()
            if (serverPort == ""){
                Toast.makeText(this,"ポート番号を入力してください", Toast.LENGTH_SHORT).show()
            } else {
                SERVER_PORT = serverPort.toInt()
                val imageByte = BitmapToByte(bitmap1)
                connect(imageByte)
            }
        }

        quitButton.setOnClickListener {
//            connect("q")
            val serverPort = portNumber.text.toString()
            if (serverPort == ""){
                Toast.makeText(this,"ポート番号を入力してください", Toast.LENGTH_SHORT).show()
            } else {
                SERVER_PORT = serverPort.toInt()
                connect("q".toByteArray())
            }
        }

        selectButton.setOnClickListener {
            val options = arrayOf<CharSequence>("Choose from Photos", "Take Picture", "Cancel")
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("New Image")

            builder.setItems(
                options
            ) { dialog: DialogInterface, item: Int ->
                if (options[item] == "Take Picture") {
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePicture, 0)
                } else if (options[item] == "Choose from Photos") {
                    val pickPhoto =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1)
                } else if (options[item] == "Cancel") {
                    dialog.dismiss()
                }
            }
            builder.show()
        }

    }

    private fun BitmapToByte(bmp: Bitmap): ByteArray{
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun imageEncode(image :Bitmap) :String{
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }


    //writer: send String data  writer1: send byte data
//    private fun connect(data: String){
    private fun connect(data: ByteArray){
        try {
            val inetSocketAddress = InetSocketAddress(SERVER_IP, SERVER_PORT)

            val task = @SuppressLint("StaticFieldLeak")
            object : AsyncTask<InetSocketAddress, Void, Void>() {
                override fun doInBackground(vararg inetSocketAddresses: InetSocketAddress): Void? {
                    var socket: Socket? = null
                    try {
                        socket = Socket()
                        socket.connect(inetSocketAddresses[0])

                        //send String
//                        val writer =
//                                BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
//                        writer.write(data)
//                        writer.flush()
//                        writer.close()
//                        socket.close()

                        //send Byte
                        val writer1 = BufferedOutputStream(socket.getOutputStream())
                        writer1.write(data)
                        writer1.flush()
                        writer1.close()
                        socket.close()

                        return null
                    } catch (e: SocketException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    return null
                }
            }
            task.execute(inetSocketAddress)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    bitmap1 = (data.extras!!["data"] as Bitmap?)!!
                    val matrix = Matrix()
                    matrix.postRotate(90.0f)
                    ImageView1.setImageBitmap(bitmap1)
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath = cursor.getString(columnIndex)
                            bitmap1 = BitmapFactory.decodeFile(picturePath)
                            val matrix = Matrix()
                            matrix.postRotate(90.0f)
                            ImageView1.setImageBitmap(bitmap1)
                            cursor.close()
                        }
                    }
                }
            }
        }
    }


}
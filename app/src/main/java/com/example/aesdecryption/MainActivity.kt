package com.example.aesdecryption

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.aesdecryption.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val aes = CBC()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            decryptButton.setOnClickListener {
                if(inputEditText.text?.isNotEmpty() == true){
                   CoroutineScope(Dispatchers.Main).launch{
                       val res = async(Dispatchers.Default){
                           aes.decrypt(inputEditText.text.toString())
                       }.await()
                       output.setText(res)
                   }
                }
                else{
                    Toast.makeText(this@MainActivity,"Fill in all information",Toast.LENGTH_SHORT).show()
                }
            }
        }
        val socket = ServerSocket(50000)
        try{
            CoroutineScope(Dispatchers.IO).launch {
                while(true){
                    val client = socket.accept()
                    val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                    val secretKey = reader.readLine()
                    val iv = reader.readLine()
                    val cipher = reader.readLine()

                    withContext(Dispatchers.Main){
                         binding.inputEditText.setText(cipher)
                    }
                    aes.setIV(iv)
                    aes.setSecretKey(secretKey)
                }
            }
        }catch(ex : Exception) {
            ex.printStackTrace()
        }

    }
}
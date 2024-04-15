package com.example.aesdecryption

fun Char.fromHexToInt() : Int {
    return if(this in '0' .. '9') this.code - '0'.code
    else this.code - 'a'.code + 10
}
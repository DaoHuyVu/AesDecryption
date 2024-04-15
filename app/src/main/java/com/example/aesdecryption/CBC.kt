package com.example.aesdecryption

import android.util.Log

class CBC {
    private lateinit var _secretKey : String
    private lateinit var iv : Array<Array<Int>>
    private var word = Array(4){Array(4){0} }
    private var numOfRound : Int = 0
    private val numOfRoundKeyMap = mapOf(16 to 44,24 to 52,32 to 60)
    fun setIV(temp : String) {
        iv = Array(4){Array(4){0} }
        for(i in 0 until 4){
            for(j in 0 until 4){
                iv[i][j] = temp[4*i+j].fromHexToInt()
            }
        }
    }

    private fun rotWord(word : Array<Int>) : Array<Int>{
        val temp = word[0]
        for(i in 0 until 3){
            word[i] = word[i+1]
        }
        word[3] = temp
        return word
    }
    fun subWord(word : Array<Int>) : Array<Int>{
        for(i in 0 until 4){
            word[i] = subByte(word[i])
        }
        return word
    }
    fun subByte(byte : Int ) : Int{
        return throughSBox(byte)
    }
    private fun throughSBox(value : Int) : Int{
        var hex = Integer.toHexString(value)
        hex = padByte(hex)
        val first = hex[0].fromHexToInt()
        val second = hex[1].fromHexToInt()
        return sBox[first][second]
    }
    fun copyWord(a : Array<Int>,b : Array<Int>) {
        for(i in 0 until 4){
            a[i] = b[i]
        }
    }
    fun rCon(word : Array<Int>,round : Int) : Array<Int>{
        word[0] = word[0] xor roundConstant[round]
        return word
    }

    fun keyExpansion(){
        val keyLength = _secretKey.length/2
        for(i in 0 until keyLength/4){
            for(j in 0 until 4){
                word[i][j] = "${_secretKey[2*(4*i+j)]}${_secretKey[2*(4*i+j)+1]}".toInt(16)
            }
        }
        for(i in keyLength/4 until numOfRoundKeyMap[keyLength]!!){
            for(j in 0 until 4){
                var temp = Array(4){0}
                copyWord(temp,word[i-1])
                if(i%(keyLength/4) == 0){
                    temp = rCon(subWord(rotWord(temp)),i/(keyLength/4))
                }
                word[i] = xorWord(temp,word[i-(keyLength/4)])
            }
        }
        for(j in 0 until 44){
            val i = 0
            Log.i("word[$] ","${word[j][i] } ${word[j][i+1] } ${word[j][i+2] } ${word[j][i+3] }")
        }
    }
    fun setSecretKey(key : String){
        _secretKey = key
        when(_secretKey.length){
            32 -> {
                numOfRound = 10
                word = Array(44){Array(4){0} }
            }
            48 -> {
                numOfRound = 12
                word = Array(52){Array(4){0} }
            }
            64 -> {
                numOfRound = 14
                word = Array(60){Array(4){0} }
            }
        }
        keyExpansion()
    }
    private fun hexToState(hex : String,blocks : Int) : Array<Array<Array<Int>>>{
        val state = Array(blocks){Array(4){Array(4){0} } }
        for(i in 0 until blocks){
            for(j in 0 until 4){
                for(k in 0 until 4){
                    state[i][j][k] = "${hex[i*32+j*8+2*k]}${hex[i*32+j*8+2*k+1]}".toInt(16)
                }
            }
        }
        return state

    }
    private fun copy(a1 : Array<Array<Int>>,a2 : Array<Array<Int>>){
        for(i in 0 until 4){
            for(j in 0 until 4){
                a2[i][j] = a1[i][j]
            }
        }
    }
    fun addRoundKey(state : Array<Array<Int>>,round : Int){
        for(i in 0 until 4){
            state[i] = xorWord(state[i], word[4*round + i])
        }
    }
    fun invSubState(state: Array<Array<Int>>){
        for(i in  0 until 4){
            for(j in 0 until 4){
                state[i][j] = invSubByte(state[i][j])
            }
        }
    }
    fun intArrayToPlainText(state : Array<Array<Array<Int>>>,blocks : Int) : String{
        var res = ""
        for(k in 0 until blocks){
            if(k == blocks - 1){
                if(state[k][3][3] <= 15){
                    var temp = state[k][3][3]
                    var isPadded = true
                    var i = 3
                    var j = 3
                    while(temp > 0){
                        if(state[k][i][j] != state[k][3][3]){
                            isPadded = false
                            break
                        }
                        j--
                        if(j == -1){
                            i--
                            j = 3
                        }
                        temp--
                    }
                    if(isPadded){
                        if(0 == state[k+1][0][0]){
                            i = 0
                            j = 0
                            temp = 0
                            while(temp < 16 - state[k][3][3]){
                                res += state[k][i][j].toChar()
                                j++
                                if(j == 4){
                                    i++
                                    j = 0
                                }
                                temp++
                            }
                            return res
                        }
                    }
                }
            }
            for(i in 0 until 4){
                for(j in 0 until 4){
                    res += state[k][i][j].toChar()
                }
            }
        }
        return res
    }
    private fun throughISBox(value : Int) : Int{
        var hex = Integer.toHexString(value)
        hex = padByte(hex)
        val first = hex[0].fromHexToInt()
        val second = hex[1].fromHexToInt()
        return iSBox[first][second]
    }
    fun invSubByte(byte : Int) : Int{
        return throughISBox(byte)
    }
    fun padByte(str : String) : String{
        var res = str
        if(str.length == 1){
            res = "0${str}"
        }
        return res
    }

    fun byteMultiplication(byte1 : Int,byte2 : Int) : Int{
        if(byte2 == 0x01) return byte1
        var res = 0
        var a = byte1
        var b = byte2
        for(i in 0 until 8){
            if(b and 0x01 == 0x01){
                res = a xor res
            }
            var temp = 0
            if(a and 0x80 == 0x80){
                temp = 0x1b
            }
            a = (a shl 1)%256 xor temp
            b = b shr 1
        }
        return res
    }
    fun invMixColumn(state : Array<Array<Int>>) : Array<Array<Int>>{
        val temp = Array(4){Array(4){0} }
        for(i in 0 until 4){
            for(j in 0 until 4){
                for(k in 0 until 4){
                    temp[j][i] = temp[j][i] xor byteMultiplication(invMixCol[i][k],state[j][k])
                }
            }
        }
        return temp
    }
    fun xorWord(word1 : Array<Int>,word2 : Array<Int>) : Array<Int>{
        for(i in 0 until 4){
            word1[i] = word1[i] xor word2[i]
        }
        return word1
    }
    fun xor4Words(state : Array<Array<Int>>,iV : Array<Array<Int>>){
        for(i in 0 until 4){
            state[i] = xorWord(state[i], iV[i])
        }
    }
    fun invShiftRow(state : Array<Array<Int>>){
        for(i in 1 until 4){
            for(k in 0 until i){
                val temp = state[3][i]
                for (j in 3 downTo 1){
                    state[j][i] = state[j-1][i]
                }
                state[0][i] = temp
            }
        }
    }

    fun decrypt(cipherText : String) : String{
        var iVTemp = iv
        val blocks = cipherText.length/16/2
        val state = hexToState(cipherText,blocks)
        for(n in 0 until blocks-1){
            // Store Cn-1
            val temp = Array(4){Array(4){0}}
            copy(state[n],temp)
            // Pn = Cn-1 xor D(Kn,Cn)
            addRoundKey(state[n],numOfRound)
            for(i in numOfRound-1 downTo 1){
                invShiftRow(state[n])
                invSubState(state[n])
                addRoundKey(state[n],i)
                state[n] = invMixColumn(state[n])
            }
            invShiftRow(state[n])
            invSubState(state[n])
            addRoundKey(state[n],0)
            xor4Words(state[n],iVTemp)
            iVTemp = temp
        }
        return intArrayToPlainText(state,blocks-1)
    }
}
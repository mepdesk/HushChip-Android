package uk.co.signstr.app.crypto

object Bech32 {
    private const val CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"
    private val CHARSET_REV = IntArray(128) { -1 }.also { arr ->
        CHARSET.forEachIndexed { i, c -> arr[c.code] = i }
    }

    private fun polymod(values: IntArray): Int {
        val gen = intArrayOf(0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3)
        var chk = 1
        for (v in values) {
            val b = chk shr 25
            chk = ((chk and 0x1ffffff) shl 5) xor v
            for (i in 0..4) {
                if ((b shr i) and 1 == 1) chk = chk xor gen[i]
            }
        }
        return chk
    }

    private fun hrpExpand(hrp: String): IntArray {
        val ret = IntArray(hrp.length * 2 + 1)
        for (i in hrp.indices) {
            ret[i] = hrp[i].code shr 5
            ret[i + hrp.length + 1] = hrp[i].code and 31
        }
        ret[hrp.length] = 0
        return ret
    }

    private fun createChecksum(hrp: String, data: IntArray): IntArray {
        val values = hrpExpand(hrp) + data + intArrayOf(0, 0, 0, 0, 0, 0)
        val pm = polymod(values) xor 1
        return IntArray(6) { (pm shr (5 * (5 - it))) and 31 }
    }

    fun encode(hrp: String, data: ByteArray): String {
        val data5 = convertBits(data, 8, 5, true)
        val checksum = createChecksum(hrp, data5)
        val sb = StringBuilder(hrp).append('1')
        for (d in data5) sb.append(CHARSET[d])
        for (c in checksum) sb.append(CHARSET[c])
        return sb.toString()
    }

    fun decode(bech32: String): Pair<String, ByteArray>? {
        val lower = bech32.lowercase()
        val pos = lower.lastIndexOf('1')
        if (pos < 1 || pos + 7 > lower.length) return null
        val hrp = lower.substring(0, pos)
        val data5 = IntArray(lower.length - pos - 1)
        for (i in data5.indices) {
            val c = lower[pos + 1 + i]
            if (c.code >= 128 || CHARSET_REV[c.code] == -1) return null
            data5[i] = CHARSET_REV[c.code]
        }
        val hrpExp = hrpExpand(hrp)
        if (polymod(hrpExp + data5) != 1) return null
        val payload = data5.copyOfRange(0, data5.size - 6)
        val data8 = convertBits(payload, 5, 8, false)
        return Pair(hrp, data8.map { it.toByte() }.toByteArray())
    }

    private fun convertBits(data: ByteArray, fromBits: Int, toBits: Int, pad: Boolean): IntArray {
        var acc = 0
        var bits = 0
        val maxv = (1 shl toBits) - 1
        val ret = mutableListOf<Int>()
        for (b in data) {
            acc = (acc shl fromBits) or (b.toInt() and 0xff)
            bits += fromBits
            while (bits >= toBits) {
                bits -= toBits
                ret.add((acc shr bits) and maxv)
            }
        }
        if (pad && bits > 0) {
            ret.add((acc shl (toBits - bits)) and maxv)
        }
        return ret.toIntArray()
    }

    private fun convertBits(data: IntArray, fromBits: Int, toBits: Int, pad: Boolean): IntArray {
        var acc = 0
        var bits = 0
        val maxv = (1 shl toBits) - 1
        val ret = mutableListOf<Int>()
        for (b in data) {
            acc = (acc shl fromBits) or b
            bits += fromBits
            while (bits >= toBits) {
                bits -= toBits
                ret.add((acc shr bits) and maxv)
            }
        }
        if (pad && bits > 0) {
            ret.add((acc shl (toBits - bits)) and maxv)
        }
        return ret.toIntArray()
    }

    fun nsecToBytes(nsec: String): ByteArray? {
        val result = decode(nsec) ?: return null
        if (result.first != "nsec" || result.second.size != 32) return null
        return result.second
    }

    fun bytesToNsec(privkey: ByteArray): String = encode("nsec", privkey)

    fun bytesToNpub(pubkey: ByteArray): String = encode("npub", pubkey)

    fun npubToBytes(npub: String): ByteArray? {
        val result = decode(npub) ?: return null
        if (result.first != "npub" || result.second.size != 32) return null
        return result.second
    }
}

package uk.co.signstr.app.crypto

import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

object Secp256k1 {
    val P = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16)
    val N = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16)
    val Gx = BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16)
    val Gy = BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16)
    val G = Point(Gx, Gy)

    data class Point(val x: BigInteger, val y: BigInteger) {
        companion object {
            val INFINITY = Point(BigInteger.ZERO, BigInteger.ZERO)
        }
        val isInfinity get() = x == BigInteger.ZERO && y == BigInteger.ZERO
    }

    fun pointAdd(p1: Point, p2: Point): Point {
        if (p1.isInfinity) return p2
        if (p2.isInfinity) return p1
        if (p1.x == p2.x && p1.y != p2.y) return Point.INFINITY
        val lam = if (p1.x == p2.x && p1.y == p2.y) {
            (BigInteger.valueOf(3) * p1.x * p1.x) * (BigInteger.TWO * p1.y).modInverse(P) % P
        } else {
            (p2.y - p1.y) * (p2.x - p1.x).modInverse(P) % P
        }
        val x3 = (lam * lam - p1.x - p2.x).mod(P)
        val y3 = (lam * (p1.x - x3) - p1.y).mod(P)
        return Point(x3, y3)
    }

    fun pointMul(p: Point, n: BigInteger): Point {
        var result = Point.INFINITY
        var current = p
        var k = n.mod(N)
        while (k > BigInteger.ZERO) {
            if (k.testBit(0)) result = pointAdd(result, current)
            current = pointAdd(current, current)
            k = k.shiftRight(1)
        }
        return result
    }

    fun getPublicKey(privkey: ByteArray): ByteArray {
        val k = BigInteger(1, privkey)
        val pub = pointMul(G, k)
        return pub.x.toByteArray32()
    }

    fun getCompressedPublicKey(privkey: ByteArray): ByteArray {
        val k = BigInteger(1, privkey)
        val pub = pointMul(G, k)
        val prefix = if (pub.y.testBit(0)) 0x03.toByte() else 0x02.toByte()
        return byteArrayOf(prefix) + pub.x.toByteArray32()
    }

    fun ecdh(privkey: ByteArray, pubkeyX: ByteArray): ByteArray {
        val k = BigInteger(1, privkey)
        val px = BigInteger(1, pubkeyX)
        val py = liftX(px) ?: throw IllegalArgumentException("Invalid public key")
        val point = Point(px, py)
        val shared = pointMul(point, k)
        return shared.x.toByteArray32()
    }

    fun liftX(x: BigInteger): BigInteger? {
        val c = (x.modPow(BigInteger.valueOf(3), P) + BigInteger.valueOf(7)).mod(P)
        val y = c.modPow((P + BigInteger.ONE).shiftRight(2), P)
        if (y.modPow(BigInteger.TWO, P) != c) return null
        return if (y.testBit(0)) P - y else y
    }

    fun generatePrivateKey(): ByteArray {
        val random = SecureRandom()
        while (true) {
            val key = ByteArray(32)
            random.nextBytes(key)
            val k = BigInteger(1, key)
            if (k > BigInteger.ONE && k < N) return key
        }
    }

    // BIP-340 Schnorr signature
    fun schnorrSign(msg: ByteArray, privkey: ByteArray): ByteArray {
        val d0 = BigInteger(1, privkey)
        val p = pointMul(G, d0)
        val d = if (p.y.testBit(0)) N - d0 else d0

        val t = xor32(d.toByteArray32(), taggedHash("BIP0340/aux", SecureRandom().let { r ->
            ByteArray(32).also { r.nextBytes(it) }
        }))
        val rand = taggedHash("BIP0340/nonce", t + p.x.toByteArray32() + msg)
        val k0 = BigInteger(1, rand).mod(N)
        if (k0 == BigInteger.ZERO) throw RuntimeException("k0 is zero")

        val r = pointMul(G, k0)
        val k = if (r.y.testBit(0)) N - k0 else k0

        val e = BigInteger(1, taggedHash("BIP0340/challenge",
            r.x.toByteArray32() + p.x.toByteArray32() + msg)).mod(N)
        val sig = r.x.toByteArray32() + (k + e * d).mod(N).toByteArray32()
        return sig
    }

    fun schnorrVerify(msg: ByteArray, pubkeyX: ByteArray, sig: ByteArray): Boolean {
        if (sig.size != 64) return false
        val px = BigInteger(1, pubkeyX)
        val py = liftX(px) ?: return false
        val p = Point(px, py)

        val r = BigInteger(1, sig.copyOfRange(0, 32))
        val s = BigInteger(1, sig.copyOfRange(32, 64))
        if (r >= P || s >= N) return false

        val e = BigInteger(1, taggedHash("BIP0340/challenge",
            sig.copyOfRange(0, 32) + pubkeyX + msg)).mod(N)
        val rPoint = pointAdd(pointMul(G, s), pointMul(p, N - e))
        if (rPoint.isInfinity) return false
        if (rPoint.y.testBit(0)) return false
        return rPoint.x == r
    }

    private fun taggedHash(tag: String, msg: ByteArray): ByteArray {
        val tagHash = sha256(tag.toByteArray(Charsets.UTF_8))
        return sha256(tagHash + tagHash + msg)
    }

    fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun xor32(a: ByteArray, b: ByteArray): ByteArray {
        val result = ByteArray(32)
        for (i in 0 until 32) result[i] = (a[i].toInt() xor b[i].toInt()).toByte()
        return result
    }
}

fun BigInteger.toByteArray32(): ByteArray {
    val bytes = this.toByteArray()
    return when {
        bytes.size == 32 -> bytes
        bytes.size > 32 -> bytes.copyOfRange(bytes.size - 32, bytes.size)
        else -> ByteArray(32 - bytes.size) + bytes
    }
}

import me.mrfang.transparent.Transparent
@JvmInline
value class Name(private val s: String) {
    fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return s.subSequence(startIndex, endIndex)
    }

    operator fun compareTo(_other: String): Int {
        return s.compareTo(_other)
    }
}

@Transparent
@JvmInline
value class Name_(private val s: String)
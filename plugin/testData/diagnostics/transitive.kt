import me.mrfang.transparent.Transparent

@JvmInline
@Transparent
value class B(private val a: A)

@JvmInline
@Transparent
value class A(private val s: String)

fun main() {
    val b = B(A("foo"))
    b.subSequence(0, 1)
}
import me.mrfang.transparent.Transparent

class Foo {
    fun <In> foo(a: In) = Unit
    fun <Out> bar(): Out = null!!
    fun <T> id(a: T): T = a
}

@Transparent
@JvmInline
value class Name(private val f: Foo)

fun main() {
    val name = Name(Foo())
    name.foo(3)
    val i = name.bar<Int>()
    val fortyTwo = name.id(42)
}

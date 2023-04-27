import me.mrfang.transparent.Transparent

class Foo {
    fun <In> foo(a: In) = Unit
    fun <InOut> bar(a: InOut): InOut = a
}

@Transparent
@JvmInline
value class Name(private val f: Foo)

fun main() {
    val name = Name(Foo())
    name.foo(3)
    name.foo("String")
    val fortyTwo = name.bar(42)
}

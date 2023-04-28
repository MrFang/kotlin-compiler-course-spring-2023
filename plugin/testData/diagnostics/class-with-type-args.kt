import me.mrfang.transparent.Transparent

abstract class Foo<out T, in S> {
    abstract fun foo(): T
    abstract fun bar(s: S): Unit
}

@Transparent
@JvmInline
value class Name(private val f: Foo<String, Int>)

fun main() {
    val name = Name(object : Foo<String, Int>() {
        override fun foo(): String = "foo"
        override fun bar(i: Int) = Unit
    })
    val s = name.foo() + "bar"
    name.bar(3)
}

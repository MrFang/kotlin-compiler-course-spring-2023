import me.mrfang.transparent.Transparent

abstract class Foo<out T> {
    abstract fun foo(): T
}

@Transparent
@JvmInline
value class Name(private val f: Foo<String>)

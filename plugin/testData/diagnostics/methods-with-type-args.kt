import me.mrfang.transparent.Transparent

abstract class Foo {
    abstract fun <In, Out> foo(a: In): Out
}

@Transparent
@JvmInline
value class Name(private val f: Foo)

import me.mrfang.transparent.Transparent

class Foo {
    fun foo(s: String) = Unit
}

@JvmInline
value class Name(private val f: Foo) {
    fun foo(s: String) = f.foo(s)
    override final fun toString() = f.toString()
}

@Transparent
@JvmInline
value class Name_(private val f: Foo)
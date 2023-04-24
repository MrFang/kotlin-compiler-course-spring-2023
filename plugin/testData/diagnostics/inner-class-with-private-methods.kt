import me.mrfang.transparent.Transparent

class Foo {
    fun _public() {}
    protected fun _protected() {}
    internal fun _internal() {}
    private fun _private() {}
}

@JvmInline
@Transparent
value class Name(private val f: Foo)
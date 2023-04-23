package me.mrfang.transparent

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.error0

object PluginErrors {
    val TRANSPARENT_WITHOUT_INLINE by error0<PsiElement>()
    val TRANSPARENT_NULLABLE_PROPERTY by error0<PsiElement>()
}

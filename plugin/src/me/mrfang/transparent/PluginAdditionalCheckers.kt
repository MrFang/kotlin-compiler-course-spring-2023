package me.mrfang.transparent

import me.mrfang.transparent.checkers.InlineTransparentChecker
import me.mrfang.transparent.checkers.TransparentPropertyNotNullChecker
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

class PluginAdditionalCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val classCheckers: Set<FirClassChecker>
            get() = setOf(InlineTransparentChecker, TransparentPropertyNotNullChecker)
    }
}

package me.mrfang.transparent.checkers

import me.mrfang.transparent.PluginErrors
import me.mrfang.transparent.isTransparent
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.utils.isInline

object TransparentInlineChecker : FirClassChecker() {
    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        if (!declaration.isTransparent(context.session)) return

        if (!declaration.isInline) {
            reporter.reportOn(declaration.source, PluginErrors.TRANSPARENT_WITHOUT_INLINE, context)
        }
    }
}

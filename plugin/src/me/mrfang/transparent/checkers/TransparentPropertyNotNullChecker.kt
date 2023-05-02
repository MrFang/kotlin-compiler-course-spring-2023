package me.mrfang.transparent.checkers

import me.mrfang.transparent.PluginErrors
import me.mrfang.transparent.isTransparent
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.canBeNull

object TransparentPropertyNotNullChecker : FirClassChecker() {
    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        if (!declaration.isTransparent(context.session)) return
        val properties = declaration.declarations.filter { it.symbol is FirPropertySymbol }

        for (prop in properties) { // There is only one property in correct declaration
            val t = (prop.symbol as FirPropertySymbol).resolvedReturnType
            if (t.canBeNull) {
                reporter.reportOn(prop.source, PluginErrors.TRANSPARENT_NULLABLE_PROPERTY, context)
            }
        }
    }
}

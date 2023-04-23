package me.mrfang.transparent.checkers

import me.mrfang.transparent.PluginErrors
import me.mrfang.transparent.isTransparent
import me.mrfang.transparent.requiredTransparentMethods
import me.mrfang.transparent.scope
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

object TransparentMethodsChecker : FirClassChecker() {
    override fun check(declaration: FirClass, context: CheckerContext, reporter: DiagnosticReporter) {
        if (!declaration.isTransparent(context.session)) return

        val properties = declaration.declarations.filter { it.symbol is FirPropertySymbol }
        val requiredMethods = declaration.symbol.requiredTransparentMethods(context.session)

        for (prop in properties) { // There is only one property in correct declaration
            val t = (prop.symbol as FirPropertySymbol).resolvedReturnType
            val scope = t.scope(context.session)
            val methods = scope.getCallableNames()

            if (!methods.containsAll(requiredMethods)) {
                reporter.reportOn(declaration.source, PluginErrors.TRANSPARENT_METHOD_NOT_EXISTS, context)
            }
        }
    }
}

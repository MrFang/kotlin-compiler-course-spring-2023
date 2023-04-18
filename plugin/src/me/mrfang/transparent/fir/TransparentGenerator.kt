package me.mrfang.transparent.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunction
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.scopes.FakeOverrideTypeCalculator
import org.jetbrains.kotlin.fir.scopes.getFunctions
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.canBeNull
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class TransparentGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    companion object {
        val TRANSPARENT_CLASS_ID = ClassId(
            FqName.fromSegments(listOf("me", "mrfang", "transparent")),
            Name.identifier("Transparent")
        )
    }

    @OptIn(SymbolInternals::class)
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val classSymbol = context!!.owner
        val property = classSymbol.declarationSymbols.find { it is FirPropertySymbol } as FirPropertySymbol
        val scope = property.resolvedReturnTypeRef.coneType.scope(
            session,
            ScopeSession(),
            FakeOverrideTypeCalculator.DoNothing,
            null
        )!!

        return scope.getFunctions(callableId.callableName)
            .map {
                buildSimpleFunction {
                    resolvePhase = FirResolvePhase.BODY_RESOLVE
                    moduleData = session.moduleData
                    origin = Key.origin
                    name = it.name
                    returnTypeRef = it.resolvedReturnTypeRef
                    valueParameters.addAll(it.valueParameterSymbols.map { it.fir })
                    typeParameters.addAll(it.typeParameterSymbols.map { it.fir })
                    status = it.rawStatus
                    symbol = FirNamedFunctionSymbol(callableId)
                    body = buildBlock {
                    }
                }
            }
            .map { it.symbol }
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        if (!classSymbol.resolvedAnnotationClassIds.contains(TRANSPARENT_CLASS_ID)) return emptySet()

        require(classSymbol.isInline) // Does inline == value?
        val property = classSymbol.declarationSymbols.find { it is FirPropertySymbol } as FirPropertySymbol
        val t = property.resolvedReturnTypeRef
        require(!t.canBeNull)
        val scope = t.coneType.scope(
            session,
            ScopeSession(),
            FakeOverrideTypeCalculator.DoNothing,
            null
        )!!

        return scope.getCallableNames()
    }

    object Key : GeneratedDeclarationKey()
}

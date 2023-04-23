package me.mrfang.transparent.fir

import me.mrfang.transparent.requiredTransparentMethods
import me.mrfang.transparent.scope
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.FirFunctionTarget
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.leastUpperBound
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildReturnExpression
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutorByMap
import org.jetbrains.kotlin.fir.scopes.getFunctions
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.canBeNull
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class TransparentGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    companion object {
        val TRANSPARENT_ANNOTATION_CLASS_ID = ClassId(
            FqName.fromSegments(listOf("me", "mrfang", "transparent")),
            Name.identifier("Transparent")
        )
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val classSymbol = context!!.owner
        val (property, t) = getPropertyWithType(classSymbol)
        val scope = t.scope(session)

        return scope.getFunctions(callableId.callableName)
            .map { functionSymbol ->
                val generatedFunction = createMemberFunction(
                    classSymbol,
                    Key,
                    callableId.callableName,
                    { typeParams ->
                        val rawMapping = typeParams.map { it.symbol to it.symbol.toConeType() }
                        val substitutor = ConeSubstitutorByMap(
                            useSiteSession = session,
                            substitution = mapOf(*rawMapping.toTypedArray())
                        )
                        substitutor.substituteOrSelf(functionSymbol.resolvedReturnType)
                    }
                ) {
                    for (symbol in functionSymbol.valueParameterSymbols) {
                        valueParameter(symbol.name, symbol.resolvedReturnType)
                    }

                    for (symbol in functionSymbol.typeParameterSymbols) {
                        typeParameter(symbol.name)
                    }
                }

                generatedFunction.replaceBody(
                    buildBlock {
                        statements.add(
                            buildReturnExpression {
                                result = buildFunctionCall {
                                    explicitReceiver = buildPropertyAccessExpression {
                                        calleeReference = buildResolvedNamedReference {
                                            name = property.name
                                            resolvedSymbol = property
                                        }
                                        typeRef = property.resolvedReturnTypeRef
                                    }
                                    typeRef = generatedFunction.returnTypeRef
                                    calleeReference = buildResolvedNamedReference {
                                        name = functionSymbol.name
                                        resolvedSymbol = functionSymbol
                                    }
                                    typeArguments.addAll(
                                        generatedFunction.typeParameters.map {
                                            buildTypeProjectionWithVariance {
                                                variance = it.variance
                                                typeRef = buildResolvedTypeRef {
                                                    type = it.symbol.toConeType()
                                                }
                                            }
                                        }
                                    )
                                    val mapping = generatedFunction.valueParameters.map {
                                        buildPropertyAccessExpression {
                                            calleeReference = buildResolvedNamedReference {
                                                name = it.name
                                                resolvedSymbol = it.symbol
                                            }
                                            typeRef = it.returnTypeRef
                                        } to it
                                    }
                                    argumentList = buildResolvedArgumentList(linkedMapOf(*mapping.toTypedArray()))
                                }
                                target = FirFunctionTarget(null, isLambda = false).apply {
                                    bind(generatedFunction)
                                }
                            }
                        )
                    }
                )

//                generatedFunction.replaceStatus(functionSymbol.rawStatus.transform(modality = Modality.FINAL))

                generatedFunction
            }
            .map { it.symbol }
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        if (!classSymbol.resolvedAnnotationClassIds.contains(TRANSPARENT_ANNOTATION_CLASS_ID)) return emptySet()
        if (!classSymbol.isInline) return emptySet()

        val (_, t) = getPropertyWithType(classSymbol)
        if (t.canBeNull) return emptySet()
        val scope = t.scope(session)

        val callableNames = scope.getCallableNames()
        val requiredMethods = classSymbol.requiredTransparentMethods(session)

        if (!callableNames.containsAll(requiredMethods)) return emptySet()

        return requiredMethods.ifEmpty { callableNames }
    }

    private fun getPropertyWithType(classSymbol: FirClassSymbol<*>): Pair<FirPropertySymbol, ConeKotlinType> {
        val property = classSymbol.declarationSymbols.find { it is FirPropertySymbol } as FirPropertySymbol
        val t = property.resolvedReturnType.leastUpperBound(session)

        return Pair(property, t)
    }

    object Key : GeneratedDeclarationKey()
}

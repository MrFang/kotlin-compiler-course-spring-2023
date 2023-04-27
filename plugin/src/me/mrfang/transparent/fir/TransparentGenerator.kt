package me.mrfang.transparent.fir

import me.mrfang.transparent.TRANSPARENT_ANNOTATION_CLASS_ID
import me.mrfang.transparent.requiredTransparentMethods
import me.mrfang.transparent.scope
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirFunctionTarget
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.leastUpperBound
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildReturnExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildThisReceiverExpression
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.references.builder.buildExplicitThisReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutorByMap
import org.jetbrains.kotlin.fir.scopes.getFunctions
import org.jetbrains.kotlin.fir.scopes.impl.toConeType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.canBeNull
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class TransparentGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {
    companion object {
        private val METHODS_TO_SKIP = listOf(Name.identifier("equals"), Name.identifier("hashCode")) // Cannot generate equals and hashCode for value class
        private val METHODS_TO_OVERRIDE = listOf(Name.identifier("toString"))
    }

//    override fun generateProperties(
//        callableId: CallableId,
//        context: MemberGenerationContext?
//    ): List<FirPropertySymbol> {
//        val classSymbol = context!!.owner
//        val (property, t) = getPropertyWithType(classSymbol)
//        val scope = t.scope(session)
//
//        return scope.getProperties(callableId.callableName)
//            .map { propertySymbol ->
//                propertySymbol as FirPropertySymbol
//                val isVar = !propertySymbol.isVal &&
//                        (propertySymbol.setterSymbol?.visibility == Visibilities.Public)
//                val isVal = !isVar
//
//                val generatedProp = createMemberProperty(
//                    owner = classSymbol,
//                    key = Key,
//                    name = callableId.callableName,
//                    returnTypeProvider = { typeParams ->
//                        val rawMapping = typeParams.map { it.symbol to it.symbol.toConeType() }
//                        val substitutor = ConeSubstitutorByMap(
//                            useSiteSession = session,
//                            substitution = mapOf(*rawMapping.toTypedArray())
//                        )
//
//                        substitutor.substituteOrSelf(propertySymbol.resolvedReturnType)
//                    },
//                    isVal = isVal,
//                    hasBackingField = propertySymbol.hasBackingField
//                )
//
//                println(generatedProp.getter)
//
//                generatedProp.replaceGetter(buildPropertyAccessor {
//                    moduleData = generatedProp.moduleData
//                    origin = FirDeclarationOrigin.Plugin(Key)
//                    status = generatedProp.status
//                    returnTypeRef = generatedProp.returnTypeRef
//                    symbol = generatedProp.symbol.getterSymbol!!
//                    this.propertySymbol = generatedProp.symbol
//                    isGetter = true
//                    // TODO: body
//                })
//                generatedProp
//            }
//            .map { it.symbol }
//    }

    @OptIn(SymbolInternals::class)
    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
        val classSymbol = context!!.owner
        val (property, t) = getPropertyWithType(classSymbol)
        val scope = t.scope(session)

        return scope.getFunctions(callableId.callableName)
            .filter { it.visibility == Visibilities.Public }
            .map { functionSymbol ->
                val generatedFunction = createMemberFunction(
                    owner = classSymbol,
                    key = Key,
                    name = callableId.callableName,
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
                        valueParameter(
                            name = symbol.name,
                            type = symbol.resolvedReturnType,
                            isVararg = symbol.isVararg
                        )
                    }

                    for (symbol in functionSymbol.typeParameterSymbols) {
                        typeParameter(symbol.name)
                    }

                    with(functionSymbol.resolvedStatus) {
                        status {
                            isInline = this@with.isInline
                            isOperator = this@with.isOperator
                            isInfix = this@with.isInfix
                            isSuspend = this@with.isSuspend
                            isOverride = functionSymbol.name in METHODS_TO_OVERRIDE
                        }
                    }

                    modality = Modality.FINAL
                }

                generatedFunction.replaceBody(
                    buildBlock {
                        statements.add(
                            buildReturnExpression {
                                result = buildFunctionCall {
                                    explicitReceiver = buildPropertyAccessExpression {
                                        explicitReceiver = buildThisReceiverExpression {
                                            val ref = buildExplicitThisReference()
                                            ref.replaceBoundSymbol(classSymbol)
                                            calleeReference = ref
                                            typeRef = buildResolvedTypeRef {
                                                type = ConeClassLikeTypeImpl(
                                                    lookupTag = classSymbol.toLookupTag(),
                                                    typeArguments = arrayOf(), // TODO
                                                    isNullable = false
                                                )
                                            }
                                        }
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

                                    val mapping = generatedFunction.valueParameters.zip(functionSymbol.valueParameterSymbols)
                                        .map { (param, symbol) ->
                                            val expression = buildPropertyAccessExpression {
                                                calleeReference = buildResolvedNamedReference {
                                                    name = param.name
                                                    resolvedSymbol = param.symbol
                                                }
                                                typeRef = param.returnTypeRef
                                            }
                                            val correspondingParam = symbol.fir

                                            Pair(expression, correspondingParam)
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

        return requiredMethods.ifEmpty { callableNames }.filter { it !in METHODS_TO_SKIP }.toSet()
    }

    private fun getPropertyWithType(classSymbol: FirClassSymbol<*>): Pair<FirPropertySymbol, ConeKotlinType> {
        val property = classSymbol.declarationSymbols.find { it is FirPropertySymbol } as FirPropertySymbol
        val t = property.resolvedReturnType.leastUpperBound(session)

        return Pair(property, t)
    }

    object Key : GeneratedDeclarationKey()
}

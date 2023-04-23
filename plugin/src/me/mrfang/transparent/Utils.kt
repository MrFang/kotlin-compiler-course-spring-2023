package me.mrfang.transparent

import me.mrfang.transparent.fir.TransparentGenerator
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.getStringArrayArgument
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.scopes.FakeOverrideTypeCalculator
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.Name

fun FirDeclaration.isTransparent(session: FirSession) =
    hasAnnotation(classId = TransparentGenerator.TRANSPARENT_ANNOTATION_CLASS_ID, session = session)

fun ConeKotlinType.scope(session: FirSession) =
    scope(session, ScopeSession(), FakeOverrideTypeCalculator.DoNothing, null)!!

fun FirClassSymbol<*>.requiredTransparentMethods(session: FirSession) =
    resolvedAnnotationsWithArguments
        .find { it.toAnnotationClassId(session) == TransparentGenerator.TRANSPARENT_ANNOTATION_CLASS_ID }!!
        .getStringArrayArgument(Name.identifier("methods"))
        ?.map(Name::identifier)
        ?.toSet() ?: setOf()

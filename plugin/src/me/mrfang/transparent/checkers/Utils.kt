package me.mrfang.transparent.checkers

import me.mrfang.transparent.fir.TransparentGenerator
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.hasAnnotation

fun FirDeclaration.isTransparent(context: CheckerContext) =
    hasAnnotation(classId = TransparentGenerator.TRANSPARENT_ANNOTATION_CLASS_ID, session = context.session)

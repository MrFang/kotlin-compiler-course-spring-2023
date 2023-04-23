package me.mrfang.transparent

import me.mrfang.transparent.fir.TransparentGenerator
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class SimplePluginRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::TransparentGenerator
        +::PluginAdditionalCheckers
    }
}

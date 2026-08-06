package com.ambhureyr.itypekeyboard.engine.rendering

class RenderEngine(
    val renderers: List<Renderer> = listOf(
        BackgroundRenderer(),
        KeyBodyRenderer(),
        KeyLabelRenderer(),
        KeyPopupRenderer(),
        GestureTrailRenderer()
    )
) {

    fun render(context: RenderContext) {
        renderers.forEach { renderer ->
            renderer.render(context)
        }
    }
}

package com.ambhureyr.itypekeyboard.engine.rendering

class RenderEngine(
    private val renderers: List<Renderer> = listOf(
        BackgroundRenderer(),
        KeyBodyRenderer(),
        KeyLabelRenderer()
    )
) {

    fun render(context: RenderContext) {
        renderers.forEach { renderer ->
            renderer.render(context)
        }
    }
}

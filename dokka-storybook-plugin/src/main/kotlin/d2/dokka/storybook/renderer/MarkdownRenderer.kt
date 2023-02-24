package d2.dokka.storybook.renderer

import d2.dokka.storybook.location.D2StorybookLocationProvider
import d2.dokka.storybook.model.render.D2TextStyle
import d2.dokka.storybook.renderer.builder.ReactFileBuilder
import org.jetbrains.dokka.base.renderers.DefaultRenderer
import org.jetbrains.dokka.base.renderers.isImage
import org.jetbrains.dokka.gfm.GfmPlugin
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.ContentBreakLine
import org.jetbrains.dokka.pages.ContentCodeBlock
import org.jetbrains.dokka.pages.ContentCodeInline
import org.jetbrains.dokka.pages.ContentDRILink
import org.jetbrains.dokka.pages.ContentDivergentGroup
import org.jetbrains.dokka.pages.ContentDivergentInstance
import org.jetbrains.dokka.pages.ContentEmbeddedResource
import org.jetbrains.dokka.pages.ContentGroup
import org.jetbrains.dokka.pages.ContentHeader
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentList
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.ContentTable
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.PlatformHintedContent
import org.jetbrains.dokka.pages.RendererSpecificPage
import org.jetbrains.dokka.pages.RenderingStrategy
import org.jetbrains.dokka.pages.Style
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.pages.hasStyle
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.query

abstract class MarkdownRenderer(
	context: DokkaContext,
): DefaultRenderer<ReactFileBuilder>(context), D2ContentRenderer {

	override lateinit var d2LocationProvider: D2StorybookLocationProvider
	override val preprocessors = context.plugin<GfmPlugin>().query { gfmPreprocessors }

	override fun buildPageContent(context: StringBuilder, page: ContentPage) {
		buildPageContent(ReactFileBuilder(context), page)
	}

	override fun buildPageContent(context: ReactFileBuilder, page: ContentPage) {
		context.buildContentNode(page.content, page)
		context.build()
	}

	override fun ReactFileBuilder.buildTable(
		node: ContentTable,
		pageContext: ContentPage,
		sourceSetRestriction: Set<DisplaySourceSet>?
	) {
		buildNewLine()
		when (node.dci.kind) {
			ContentKind.Properties -> buildTableProperties(node, pageContext)
			ContentKind.Functions -> buildTableFunctions(node, pageContext)
			else -> Unit
		}
	}
	protected abstract fun ReactFileBuilder.buildTableProperties(node: ContentTable, pageContext: ContentPage)
	protected abstract fun ReactFileBuilder.buildTableFunctions(node: ContentTable, pageContext: ContentPage)

	protected open fun ReactFileBuilder.buildNewLine() {
		append("\n")
	}

	override fun ReactFileBuilder.buildText(textNode: ContentText) {
		if (textNode.text.isNotBlank()) {
			val decorators = decorators(textNode.style)
			append(textNode.text.takeWhile { it == ' ' })
			append(decorators)
			append(textNode.text.trim())
			append(decorators.reversed())
			append(textNode.text.takeLastWhile { it == ' ' })
		}
	}

	protected open fun decorators(styles: Set<Style>) = buildString {
		styles.forEach { style ->
			when (style) {
				TextStyle.Bold -> append("**")
				TextStyle.Italic -> append("*")
				TextStyle.Strong -> append("**")
				TextStyle.Strikethrough -> append("~~")
				D2TextStyle.Code -> append("`")
				else -> Unit
			}
		}
	}

	override fun ReactFileBuilder.buildDRILink(
		node: ContentDRILink,
		pageContext: ContentPage,
		sourceSetRestriction: Set<DisplaySourceSet>?
	) {
		d2LocationProvider.resolveAnchor(node.address, pageContext)?.let {
			buildLink(it) {
				buildText(node.children, pageContext, sourceSetRestriction)
			}
		} ?: buildText(node.children, pageContext, sourceSetRestriction)
	}

	override fun ReactFileBuilder.buildList(
		node: ContentList,
		pageContext: ContentPage,
		sourceSetRestriction: Set<DisplaySourceSet>?
	) {
		node.children.forEach { child ->
			append(" - ")
			append(buildString { child.build(ReactFileBuilder(this), pageContext, sourceSetRestriction) }.trim())
			append("\n")
		}
	}


	// ======================= Copied from CommonMarkRenderer =======================
	// (will be rewritten bit by bit when/if the need arise)


	override fun ReactFileBuilder.wrapGroup(
		node: ContentGroup,
		pageContext: ContentPage,
		childrenCallback: ReactFileBuilder.() -> Unit
	) {
		return when {
			node.hasStyle(TextStyle.Block) || node.hasStyle(TextStyle.Paragraph) -> {
				buildParagraph()
				childrenCallback()
				buildParagraph()
			}
			node.dci.kind == ContentKind.Deprecation -> {
				append("---")
				childrenCallback()
				append("---")
				buildNewLine()
			}
			node.hasStyle(ContentStyle.Footnote) -> {
				childrenCallback()
				buildParagraph()
			}
			else -> childrenCallback()
		}
	}

	override fun ReactFileBuilder.buildHeader(level: Int, node: ContentHeader, content: ReactFileBuilder.() -> Unit) {
		buildParagraph()
		append("#".repeat(level) + " ")
		content()
		buildParagraph()
	}

	override fun ReactFileBuilder.buildLink(address: String, content: ReactFileBuilder.() -> Unit) {
		append("[")
		content()
		append("]($address)")
	}

	private fun ReactFileBuilder.buildList(
		node: ContentList,
		pageContext: ContentPage
	) {
		node.children.forEachIndexed { i, it ->
			if (node.ordered) {
				// number is irrelevant, but a nice touch
				// period is more widely compatible
				append("${i + 1}. ")
			} else {
				append("- ")
			}

			/*
            Handle case when list item transitions to another complex node with no preceding text.
            For example, the equivalent of:
            <li>
               <ul><li><ul>Item</ul></li></ul>
            </li>

            Would be:
            -
               - Item
             */
			if (it is ContentGroup && it.children.firstOrNull()?.let { it !is ContentText } == true) {
				append("\n   ")
			}

			buildString { it.build(ReactFileBuilder(this), pageContext, it.sourceSets) }
				.replace("\n", "\n   ") // apply indent
				.trim().let { append(it) }
			buildNewLine()
		}
	}

	override fun ReactFileBuilder.buildLineBreak() {
		append("  ")
		buildNewLine()
	}

	private fun ReactFileBuilder.buildParagraph() {
		buildNewLine()
		buildNewLine()
	}

	override fun ReactFileBuilder.buildPlatformDependent(
		content: PlatformHintedContent,
		pageContext: ContentPage,
		sourceSetRestriction: Set<DisplaySourceSet>?
	) {
		buildPlatformDependentItem(content.inner, content.sourceSets, pageContext)
	}

	private fun ReactFileBuilder.buildPlatformDependentItem(
		content: ContentNode,
		sourceSets: Set<DisplaySourceSet>,
		pageContext: ContentPage,
	) {
		if (content is ContentGroup && content.children.firstOrNull { it is ContentTable } != null) {
			buildContentNode(content, pageContext, sourceSets)
		} else {
			val distinct = sourceSets.map {
				it to buildString { buildContentNode(content, pageContext, setOf(it)) }
			}.groupBy(Pair<DisplaySourceSet, String>::second, Pair<DisplaySourceSet, String>::first)

			distinct.filter { it.key.isNotBlank() }.forEach { (text, platforms) ->
				buildParagraph()
				buildSourceSetTags(platforms.toSet())
				buildLineBreak()
				append(text.trim())
				buildParagraph()
			}
		}
	}

	override fun ReactFileBuilder.buildResource(node: ContentEmbeddedResource, pageContext: ContentPage) {
		if (node.isImage()) {
			append("!")
		}
		append("[${node.altText}](${node.address})")
	}

	override fun ReactFileBuilder.buildNavigation(page: PageNode) {
		locationProvider.ancestors(page).asReversed().forEach { node ->
			append("/")
			if (node.isNavigable) buildLink(node, page)
			else append(node.name)
		}
		buildParagraph()
	}

	override fun buildPage(page: ContentPage, content: (ReactFileBuilder, ContentPage) -> Unit): String =
		buildString {
			content(ReactFileBuilder(this), page)
		}.trim().replace("\n[\n]+".toRegex(), "\n\n")

	override fun buildError(node: ContentNode) {
		context.logger.warn("Markdown renderer has encountered problem. The unmatched node is $node")
	}

	override fun ReactFileBuilder.buildDivergent(node: ContentDivergentGroup, pageContext: ContentPage) {

		val distinct =
			node.groupDivergentInstances(pageContext, { instance, _, sourceSet ->
				instance.before?.let { before ->
					buildString { buildContentNode(before, pageContext, sourceSet) }
				} ?: ""
			}, { instance, _, sourceSet ->
				instance.after?.let { after ->
					buildString { buildContentNode(after, pageContext, sourceSet) }
				} ?: ""
			})

		distinct.values.forEach { entry ->
			val (instance, sourceSets) = entry.getInstanceAndSourceSets()

			buildParagraph()
			buildSourceSetTags(sourceSets)
			buildLineBreak()

			instance.before?.let {
				buildContentNode(
					it,
					pageContext,
					sourceSets.first()
				) // It's workaround to render content only once
				buildParagraph()
			}

			entry.groupBy { buildString { buildContentNode(it.first.divergent, pageContext, setOf(it.second)) } }
				.values.forEach { innerEntry ->
					val (innerInstance, innerSourceSets) = innerEntry.getInstanceAndSourceSets()
					if (sourceSets.size > 1) {
						buildSourceSetTags(innerSourceSets)
						buildLineBreak()
					}
					innerInstance.divergent.build(
						this@buildDivergent,
						pageContext,
						setOf(innerSourceSets.first())
					) // It's workaround to render content only once
					buildParagraph()
				}

			instance.after?.let {
				buildContentNode(
					it,
					pageContext,
					sourceSets.first()
				) // It's workaround to render content only once
			}

			buildParagraph()
		}
	}

	override fun ReactFileBuilder.buildCodeBlock(code: ContentCodeBlock, pageContext: ContentPage) {
		append("```")
		append(code.language.ifEmpty { "kotlin" })
		buildNewLine()
		code.children.forEach {
			if (it is ContentText) {
				// since this is a code block where text will be rendered as is,
				// no need to escape text, apply styles, etc. Just need the plain value
				append(it.text)
			} else if (it is ContentBreakLine) {
				// since this is a code block where text will be rendered as is,
				// there's no need to add tailing slash for line breaks
				buildNewLine()
			}
		}
		buildNewLine()
		append("```")
		buildNewLine()
	}

	override fun ReactFileBuilder.buildCodeInline(code: ContentCodeInline, pageContext: ContentPage) {
		append("`")
		code.children.filterIsInstance<ContentText>().forEach { append(it.text) }
		append("`")
	}

	private val PageNode.isNavigable: Boolean
		get() = this !is RendererSpecificPage || strategy != RenderingStrategy.DoNothing

	private fun ReactFileBuilder.buildLink(to: PageNode, from: PageNode) =
		buildLink(locationProvider.resolve(to, from)!!) {
			append(to.name)
		}

	private fun List<Pair<ContentDivergentInstance, DisplaySourceSet>>.getInstanceAndSourceSets() =
		this.let { Pair(it.first().first, it.map { it.second }.toSet()) }

	private fun ReactFileBuilder.buildSourceSetTags(sourceSets: Set<DisplaySourceSet>) =
		append(sourceSets.joinToString(prefix = "[", postfix = "]") { it.name })
}

package d2.dokka.storybook.translators

import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.base.signatures.SignatureProvider
import org.jetbrains.dokka.base.transformers.pages.comments.CommentsToContentConverter
import org.jetbrains.dokka.base.translators.documentables.DefaultPageCreator
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.pages.ClasslikePageNode
import org.jetbrains.dokka.pages.ModulePageNode
import org.jetbrains.dokka.utilities.DokkaLogger

class D2StorybookPageCreator(
    configuration: DokkaBaseConfiguration?,
    commentsToContentConverter: CommentsToContentConverter,
    signatureProvider: SignatureProvider,
    logger: DokkaLogger
): DefaultPageCreator(configuration, commentsToContentConverter, signatureProvider, logger) {

    override fun pageForModule(m: DModule) =
        ModulePageNode(m.name.ifEmpty { "<root>" }, contentForModule(m), m, m.packages.flatMap(DPackage::classlikes).map(::pageForClasslike))

    override fun pageForClasslike(c: DClasslike): ClasslikePageNode {
        return ClasslikePageNode(
            c.name.orEmpty(), contentForClasslike(c), setOf(), null, emptyList()
        )
    }
}

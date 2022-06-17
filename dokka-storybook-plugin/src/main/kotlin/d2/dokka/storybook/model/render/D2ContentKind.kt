package d2.dokka.storybook.model.render

import org.jetbrains.dokka.pages.Kind

/**
 * Represents a grouping of content of one kind.
 * Usually used to describe what kind of data a ContentNode contains.
 */
enum class D2ContentKind: Kind {
    /**
     * Contains the D2 children of the documentable associated with the node.
     */
    Children,

    /**
     * Marks a node as a simple container for other nodes.
     */
    Container,

    /**
     * Contains description data
     */
    Description,

    /**
     * Contains a file
     */
    File,

    /**
     * Contains D2 description and/or visual file(s) of the documentable associated with the node.
     */
    Source,

    /**
     * Contains Visual data
     */
    Visual
}

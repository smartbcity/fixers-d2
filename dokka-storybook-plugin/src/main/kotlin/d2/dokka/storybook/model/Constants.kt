package d2.dokka.storybook.model

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.links.PointingToDeclaration

object Constants {
    object Annotation {
        val ROLES_ALLOWED = DRI(
            packageName = "javax.annotation.security",
            classNames = "RolesAllowed",
            target = PointingToDeclaration
        )
        val PERMIT_ALL = DRI(
            packageName = "javax.annotation.security",
            classNames = "PermitAll",
            target = PointingToDeclaration
        )
    }
}

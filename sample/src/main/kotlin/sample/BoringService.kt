package sample

import f2.dsl.fnc.F2Consumer
import f2.dsl.fnc.F2Function
import f2.dsl.fnc.F2Supplier
import f2.dsl.fnc.f2Consumer
import f2.dsl.fnc.f2Function
import f2.dsl.fnc.f2SupplierSingle
import javax.annotation.security.PermitAll
import javax.annotation.security.RolesAllowed

typealias BoringFunction = F2Function<BoringInterface, Boolean>
typealias BoringBoringFunction = BoringFunction

/**
 * Does some boring stuff
 * @d2 service
 */
class BoringService {
    /**
     * So boring, can't help but sleep
     * @return true if slept, false else
     */
    fun sleep(duration: Long): Boolean = duration > 0

    /**
     * Doesn't even bother to do anything
     */
    fun procrastinate(query: String) {}

    /**
     * Would consume stuff if it weren't lazy
     */
    @RolesAllowed("get_stuff", "consume_stuff")
    fun consume(): F2Consumer<Long> = f2Consumer {  }

    /**
     * Probably does stuff but it's not really interesting
     */
    @PermitAll
    fun doStuff(): BoringBoringFunction = f2Function { true }

    /**
     * Supplies a useless not-ever-changing indicator
     */
    fun supply(): F2Supplier<Boolean> = f2SupplierSingle { true }

    /**
     * Annoying function
     */
    fun getBoring(): BoringGetQueryFunction = f2Function { TODO() }
}

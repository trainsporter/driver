package lol.adel.driver

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.generateViewId
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.AutoTransitionChangeHandler
import kotlinx.coroutines.experimental.channels.consumeEach
import lol.adel.driver.screens.IdleController
import lol.adel.driver.screens.OrderController
import org.jetbrains.anko.act
import org.jetbrains.anko.find

object Ids {
    val map = generateViewId()
    val mapParent = generateViewId()
    val mapPanel = generateViewId()
}

fun Screen.toController(): Controller =
    when (this) {
        Screen.Idle -> IdleController()
        Screen.Order -> OrderController()
    }

class MainActivity : AppCompatActivity() {

    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (currentUserId() == null) {
            startActivityForResult(
                loginIntent(),
                1234
            )
        }

        router = Conductor.attachRouter(act, find(android.R.id.content), savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(init().second.toController()))
        }

        untilDestroy {
            StateContainer.navs.openSubscription().distinctUntilChanged().consumeEach { nav ->
                when (nav) {
                    is Nav.Push ->
                        router.pushController(RouterTransaction.with(nav.screen.toController()))

                    Nav.Pop ->
                        router.popCurrentController()

                    is Nav.ReplaceTop ->
                        router.replaceTopController(RouterTransaction.with(nav.screen.toController()))

                    Nav.NoOp ->
                        Unit

                    is Nav.Reset -> {
                        val txs = nav.screens.map { RouterTransaction.with(it.toController()) }
                        router.setBackstack(txs, AutoTransitionChangeHandler())
                    }
                }
            }
        }
    }

    override fun onBackPressed(): Unit =
        when {
            router.handleBack() ->
                Unit

            else ->
                super.onBackPressed()
        }
}

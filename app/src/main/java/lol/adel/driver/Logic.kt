package lol.adel.driver

sealed class Model {
    object Offline : Model()
    object Idle : Model()
    data class ActiveOrder(val order: Order) : Model()
}

sealed class Msg {
    object GoOffline : Msg()
    object GoOnline : Msg()
    data class OrderAvailable(val order: Order) : Msg()
}

sealed class Screen {
    object Idle : Screen()
    object Order : Screen()
}

sealed class Nav {
    data class Push(val screen: Screen) : Nav()
    object Pop : Nav()
    data class ReplaceTop(val screen: Screen) : Nav()
    object NoOp : Nav()
    data class Reset(val screens: List<Screen>) : Nav()
}

fun init(): Pair<Model, Screen> =
    Model.Offline to Screen.Idle

fun update(model: Model, msg: Msg): Pair<Model, Nav> =
    when (msg) {
        Msg.GoOffline ->
            Model.Offline to Nav.Reset(listOf(Screen.Idle))

        Msg.GoOnline ->
            Model.Idle to Nav.NoOp

        is Msg.OrderAvailable ->
            when (model) {
                Model.Offline ->
                    model to Nav.NoOp

                Model.Idle, is Model.ActiveOrder ->
                    Model.ActiveOrder(msg.order) to Nav.Reset(listOf(Screen.Order))
            }
    }

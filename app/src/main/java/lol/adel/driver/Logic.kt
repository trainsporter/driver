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

fun update(model: Model, msg: Msg): Model =
    when (msg) {
        Msg.GoOffline ->
            Model.Offline

        Msg.GoOnline ->
            Model.Idle

        is Msg.OrderAvailable ->
            when (model) {
                Model.Offline ->
                    model

                Model.Idle, is Model.ActiveOrder ->
                    Model.ActiveOrder(msg.order)
            }
    }

sealed class NavigatorViewModel {
    object Idle : NavigatorViewModel()
    object Map : NavigatorViewModel()
}

fun navigator(model: Model): NavigatorViewModel =
    when (model) {
        Model.Offline, Model.Idle ->
            NavigatorViewModel.Idle

        is Model.ActiveOrder ->
            NavigatorViewModel.Map
    }

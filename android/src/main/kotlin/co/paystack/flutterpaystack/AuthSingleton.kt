package co.paystack.flutterpaystack

class AuthSingleton private constructor() {

    @Volatile
    var responseJson: String = "{\"status\":\"requery\",\"message\":\"Reaffirm Transaction Status on Server\"}"
        @Synchronized set

    @Volatile
    var url: String = ""
        @Synchronized set

    companion object {
        @Volatile
        private var INSTANCE: AuthSingleton? = null

        val instance: AuthSingleton
            get() {
                return INSTANCE ?: synchronized(this) {
                    val newInstance = INSTANCE ?: AuthSingleton()
                    INSTANCE = newInstance
                    newInstance
                }
            }
    }
}

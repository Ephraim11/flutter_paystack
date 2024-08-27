package co.paystack.flutterpaystack

/**
 * Created by Wilberforce on 29/07/18 at 06:00.
 */
class AuthSingleton private constructor() {

    @Volatile
    var responseJson: String = "{\"status\":\"requery\",\"message\":\"Reaffirm Transaction Status on Server\"}"
    var url: String = ""

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

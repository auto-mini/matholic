package com.local.matholickiosk.webpoc

class Gate3RunSession(
    accountA: Gate3AccountInput,
    accountB: Gate3AccountInput,
    val targetCycles: Int = DEFAULT_TARGET_CYCLES,
) {
    private val accounts = arrayOf(
        RuntimeAccount(accountA),
        RuntimeAccount(accountB),
    )
    private var wiped = false

    var completedCycles: Int = 0
        private set

    init {
        require(targetCycles > 0)
    }

    val isComplete: Boolean
        get() = completedCycles >= targetCycles

    val nextSlotLabel: String
        get() = if (completedCycles % 2 == 0) "A" else "B"

    fun nextAttempt(): Gate3Attempt {
        check(!wiped)
        check(!isComplete)
        val account = accounts[completedCycles % accounts.size]
        return Gate3Attempt(
            slotLabel = nextSlotLabel,
            expectedDisplayName = account.expectedNameForImmediateUse(),
            credentials = account.credentialsForImmediateUse(),
        )
    }

    fun recordCompletedCycle() {
        check(!wiped)
        check(!isComplete)
        completedCycles += 1
    }

    fun wipe() {
        if (wiped) return
        accounts.forEach { it.wipe() }
        wiped = true
    }

    private class RuntimeAccount(input: Gate3AccountInput) {
        private val expectedName = input.expectedDisplayName.toCharArray()
        private val username = input.username.toCharArray()
        private val password = input.password.toCharArray()

        fun expectedNameForImmediateUse(): String = String(expectedName)

        fun credentialsForImmediateUse(): EphemeralCredentials =
            EphemeralCredentials(String(username), String(password))

        fun wipe() {
            expectedName.fill('\u0000')
            username.fill('\u0000')
            password.fill('\u0000')
        }
    }

    companion object {
        const val DEFAULT_TARGET_CYCLES = 100
    }
}

class Gate3AccountInput(
    val expectedDisplayName: String,
    val username: String,
    val password: String,
)

class Gate3Attempt(
    val slotLabel: String,
    val expectedDisplayName: String,
    val credentials: EphemeralCredentials,
)

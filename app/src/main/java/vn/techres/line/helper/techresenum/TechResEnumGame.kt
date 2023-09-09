package vn.techres.line.helper.techresenum

enum class TechResEnumGame {
    /**
     * Lucky Wheel
     * */
    JOIN_LUCKY_WHEEL_ROOM {
        override fun toString(): String {
            return "join-lucky-wheel-room"
        }
    },
    REACTION_GAME {
        override fun toString(): String {
            return "reaction-game"
        }
    },
    CHAT_TEXT_EMOJI {
        override fun toString(): String {
            return "chat-text-emoji"
        }
    },
    CHAT_STICKER {
        override fun toString(): String {
            return "chat-sticker"
        }
    },
    LEAVE_LUCKY_WHEEL_ROOM {
        override fun toString(): String {
            return "leave-lucky-wheel-room"
        }
    },
    RES_LUCKY_NUMBER {
        override fun toString(): String {
            return "res-lucky-number"
        }
    },
    RES_START {
        override fun toString(): String {
            return "res-start"
        }
    },
    RES_STOP {
        override fun toString(): String {
            return "res-stop"
        }
    },
    RES_CHAT_TEXT_EMOJI {
        override fun toString(): String {
            return "res-chat-text-emoji"
        }
    },
    RES_CHAT_SICKER {
        override fun toString(): String {
            return "res-chat-sticker"
        }
    },
    RES_TOTAL_CUSTOMER_JOIN_ROOM {
        override fun toString(): String {
            return "res-total-customer-join-room"
        }
    },
    RES_REACTION_GAME {
        override fun toString(): String {
            return "res-reaction-game"
        }
    },
    RES_CURRENT_LUCKY_WHEEL_TIME {
        override fun toString(): String {
            return "res-current-luckywheel-time"
        }
    },
    RES_NEXT_MONEY {
        override fun toString(): String {
            return "res-next-money"
        }
    },
    RES_CURRENT_MONEY {
        override fun toString(): String {
            return "res-current-money"
        }
    },
    RES_ROOM_IS_PLAYING {
        override fun toString(): String {
            return "res-room-is-playing"
        }
    },
}
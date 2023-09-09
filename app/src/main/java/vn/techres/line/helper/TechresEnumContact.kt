package vn.techres.line.helper

enum class TechresEnumContact {
    SAVE_LIST_FRIEND{
        override fun toString(): String {
            return "SAVE_LIST_FRIEND"
        }
    },
    SAVE_FRIEND_NEW{
        override fun toString(): String {
            return "SAVE_FRIEND_NEW"
        }
    },
    SAVE_FRIEND_ONLINE{
        override fun toString(): String {
            return "SAVE_FRIEND_NEW"
        }
    },
    SAVE_BEST_FRIEND{
        override fun toString(): String {
            return "SAVE_BEST_FRIEND"
        }
    },
    SAVE_FRIEND_TOTAL_REQUEST{
        override fun toString(): String {
            return "SAVE_FRIEND_TOTAL_REQUEST"
        }
    },
}
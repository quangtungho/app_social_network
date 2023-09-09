package vn.techres.line.helper

enum class NotificationEnum {
    POINT {
        override fun toString(): String {
            return "2"
        }
    },
    CUSTOMER_BALANCE {
        override fun toString(): String {
            return "6"
        }
    },
    ADVERT {
        override fun toString(): String {
            return "7"
        }
    },
    BIRTHDAY_CONGRATULATION {
        override fun toString(): String {
            return "9"
        }
    },
    TAKECARE_CUSTOMER {
        override fun toString(): String {
            return "10"
        }
    },
    REACTIONS {
        override fun toString(): String {
            return "11"
        }
    },
    COMMENTS {
        override fun toString(): String {
            return "12"
        }
    },
    CREATE_BRANCH_REVIEW {
        override fun toString(): String {
            return "13"
        }
    },
    REPLY_COMMENT {
        override fun toString(): String {
            return "14"
        }
    },
    REACH_POINT_ALOLINE {
        override fun toString(): String {
            return "15"
        }
    },
    CONTACT {
        override fun toString(): String {
            return "16"
        }
    },
    CHAT {
        override fun toString(): String {
            return "17"
        }
    },
    VIDEO_CALL {
        override fun toString(): String {
            return "18"
        }
    },
    UPDATE_INFO {
        override fun toString(): String {
            return "19"
        }
    },
    REACTIONS_COMMENT {
        override fun toString(): String {
            return "20"
        }
    },
    ORDER {
        override fun toString(): String {
            return "22"
        }
    },
    TAKECARE_CUSTOMER_NOT_GIFT {
        override fun toString(): String {
            return "30"
        }
    },
    BOOKING {
        override fun toString(): String {
            return "31"
        }
    },
    REGISTER_MEMBERSHIP_CARD {
        override fun toString(): String {
            return "32"
        }
    },
    ADVERT_MANAGER {
        override fun toString(): String {
            return "33"
        }
    },
}